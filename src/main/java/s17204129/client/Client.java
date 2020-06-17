package s17204129.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import s17204129.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Paths;

public class Client extends Application {

    @Override
    public void start(final Stage primaryStage) {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10));

        final TableView<UploadTask> tableView = new TableView<>();
        TableColumn<UploadTask, String> filenameColumn = new TableColumn<>("文件名");
        filenameColumn.setCellValueFactory(new PropertyValueFactory<UploadTask, String>("filename"));
        TableColumn<UploadTask, Double> progressColumn = new TableColumn<>("上传进度");
        progressColumn.setCellValueFactory(new PropertyValueFactory<UploadTask, Double>("progress"));
        progressColumn.setCellFactory(ProgressBarTableCell.<UploadTask>forTableColumn());
        TableColumn<UploadTask, String> speedColumn = new TableColumn<>("上传速度");
        speedColumn.setCellValueFactory(new PropertyValueFactory<UploadTask, String>("speed"));
        tableView.getColumns().add(filenameColumn);
        tableView.getColumns().add(speedColumn);
        tableView.getColumns().add(progressColumn);
        gridPane.add(tableView, 0, 0, 4, 1);

        Label baseUrlLabel = new Label("上传地址：");
        gridPane.add(baseUrlLabel, 0, 1);

        final TextField baseUrlField = new TextField();
        baseUrlField.setPromptText("http://localhost/");
        gridPane.add(baseUrlField, 1, 1, 3, 1);

        Label filenameLabel = new Label("文件路径：");
        gridPane.add(filenameLabel, 0, 2);

        final TextField filenameTextField = new TextField();
        gridPane.add(filenameTextField, 1, 2);

        Button selectButton = new Button("选择文件...");
        gridPane.add(selectButton, 2, 2);

        final Button uploadButton = new Button("上传");
        uploadButton.setDisable(true);
        gridPane.add(uploadButton, 3, 2);

        final Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        gridPane.add(errorLabel, 0, 3, 4, 1);

        selectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    filenameTextField.setText(file.getAbsolutePath());
                }
            }
        });

        filenameTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                uploadButton.setDisable(newValue.length() == 0);
            }
        });

        uploadButton.setOnAction(new EventHandler<ActionEvent>() {

            private SSLSocketFactory sslSocketFactory = null;

            @Override
            public void handle(ActionEvent event) {
                uploadButton.setDisable(true);
                try {
                    String baseUrl = baseUrlField.getText();
                    String filename = filenameTextField.getText();
                    final FileInputStream inputStream = new FileInputStream(filename);
                    final int available = inputStream.available();
                    if (baseUrl.trim().length() == 0) {
                        baseUrl = "http://localhost/";
                    }
                    URI uri = URI.create(baseUrl);
                    String scheme = uri.getScheme();
                    if (scheme == null) {
                        scheme = "http";
                    }
                    scheme = scheme.toLowerCase();
                    int port = uri.getPort();
                    if (port == -1) {
                        switch (scheme) {
                            case "http": {
                                port = 80;
                                break;
                            }
                            case "https": {
                                port = 443;
                                break;
                            }
                            default: {
                                throw new Exception("不支持的协议：" + scheme);
                            }
                        }
                    }
                    String host = uri.getHost();
                    InetAddress[] addresses = host == null ? new InetAddress[]{InetAddress.getLocalHost()} : InetAddress.getAllByName(host);
                    String path = uri.getPath();
                    if (path == null) {
                        path = "/";
                    }
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    path += Paths.get(filename).getFileName();
                    for (int i = 0; i < addresses.length; i++) {
                        try {
                            final Socket socket;
                            if (scheme.equals("http")) {
                                socket = new Socket(addresses[i], port);
                            } else {
                                if (sslSocketFactory == null) {
                                    sslSocketFactory = SSLContext.getDefault().getSocketFactory();
                                }
                                socket = sslSocketFactory.createSocket(addresses[i], port);
                            }
                            final HttpRequestHeader requestHeader = new HttpRequestHeader();
                            requestHeader.getHeaders().put(SupportedHttpHeaders.CONTENT_LENGTH, Integer.toString(available));
                            requestHeader.setMethod(SupportedHttpMethods.PUT);
                            URI requestUri = URI.create(scheme + "://" + host + ":" + port + path);
                            requestHeader.setRequestUri(requestUri);
                            final UploadTask uploadTask = new UploadTask();
                            tableView.getItems().add(uploadTask);
                            uploadTask.filenameProperty().setValue(filename);
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        HttpClientConnection clientConnection = new HttpClientConnection(socket) {

                                            private long lastTime = System.currentTimeMillis();

                                            @Override
                                            protected void onProgress(final int bytesThisTransfer, final int bytesTransferred, final int bytesTotal) {
                                                long currentTime = System.currentTimeMillis();
                                                double bytesPerSecond = bytesThisTransfer / (double) (currentTime - lastTime) / 1000;
                                                final String speed = bytesPerSecond < 1000 ? String.format("%.1f kB/s", bytesPerSecond) : String.format("%.1f MB/s", bytesPerSecond / 1000);
                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        uploadTask.speedProperty().setValue(speed);
                                                        uploadTask.progressProperty().setValue(bytesTotal == 0 ? 0 : bytesTransferred / bytesTotal);
                                                    }
                                                });
                                                lastTime = currentTime;
                                            }
                                        };
                                        HttpResponseHeader responseHeader = clientConnection.sendRequest(requestHeader, inputStream, available);
                                        byte[] buffer = new byte[clientConnection.getInputStream().available()];
                                        assert clientConnection.getInputStream().read(buffer) == buffer.length;
                                        System.out.print(responseHeader);
                                        System.out.print(new String(buffer));
                                    } catch (final Exception e) {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                errorLabel.setText(e.getLocalizedMessage());
                                            }
                                        });
                                    }
                                }
                            };
                            thread.start();
                            break;
                        } catch (IOException e) {
                            if (i == addresses.length - 1) {
                                throw e;
                            }
                        }
                    }
                } catch (Exception e) {
                    errorLabel.setText(e.toString());
                }
                uploadButton.setDisable(false);
            }
        });

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        primaryStage.setResizable(false);
        primaryStage.setTitle("上传客户端");
        primaryStage.setScene(new Scene(gridPane));
        primaryStage.show();
    }

    public static class UploadTask {

        private final StringProperty filename = new SimpleStringProperty();

        private final DoubleProperty progress = new SimpleDoubleProperty();

        private final StringProperty speed = new SimpleStringProperty();

        public StringProperty filenameProperty() {
            return filename;
        }

        public DoubleProperty progressProperty() {
            return progress;
        }

        public StringProperty speedProperty() {
            return speed;
        }
    }
}
