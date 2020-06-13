package s17204129.server;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import s17204129.HttpServer;
import s17204129.HttpsServer;

import java.io.File;

public class Server extends Application {

    private HttpServer httpServer;

    private HttpsServer httpsServer;

    @Override
    public void start(final Stage primaryStage) {
        ButtonBar buttonBar = new ButtonBar();
        final Button startButton = new Button("启动服务");
        startButton.setDisable(true);
        final Button stopButton = new Button("停止服务");
        stopButton.setDisable(true);

        final Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        Label webRootLabel = new Label("工作目录：");
        gridPane.add(webRootLabel, 0, 0);
        final TextField webRootTextField = new TextField(".");
        gridPane.add(webRootTextField, 1, 0);
        Button webRootSelectionButton = new Button("...");
        webRootSelectionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File file = directoryChooser.showDialog(primaryStage);
                if (file != null) {
                    webRootTextField.setText(file.getAbsolutePath());
                }
            }
        });
        gridPane.add(webRootSelectionButton, 2, 0);
        Label portLabel = new Label("HTTP 端口：");
        gridPane.add(portLabel, 0, 1);
        final TextField portTextField = new TextField("80");
        setNumberField(portTextField);
        gridPane.add(portTextField, 1, 1);
        Label sslPortLabel = new Label("HTTPS 端口：");
        gridPane.add(sslPortLabel, 0, 2);
        final TextField sslPortTextField = new TextField("443");
        setNumberField(sslPortTextField);
        gridPane.add(sslPortTextField, 1, 2);
        Label sslProtocolLabel = new Label("SSL 协议类型：");
        gridPane.add(sslProtocolLabel, 0, 3);
        final ComboBox<String> sslProtocolComboBox = new ComboBox<>();
        sslProtocolComboBox.getItems().addAll("SSL", "SSLv1", "SSLv2", "SSLv3", "TLS", "TLSv1.1", "TLSv1.2");
        sslProtocolComboBox.setValue("TLSv1.2");
        gridPane.add(sslProtocolComboBox, 1, 3);
        Label keystoreTypeLabel = new Label("证书格式：");
        gridPane.add(keystoreTypeLabel, 0, 4);
        final ComboBox<String> keystoreTypeComboBox = new ComboBox<>();
        keystoreTypeComboBox.getItems().addAll("JCEKS", "JKS", "PKCS12");
        keystoreTypeComboBox.setValue("JKS");
        gridPane.add(keystoreTypeComboBox, 1, 4);
        final Label keystoreFile = new Label("证书私钥文件：");
        gridPane.add(keystoreFile, 0, 5);
        final TextField keystoreFileTextField = new TextField();
        keystoreFileTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                startButton.setDisable(newValue.trim().length() == 0);
            }
        });
        gridPane.add(keystoreFileTextField, 1, 5);
        Button keystoreFileSelectButton = new Button("...");
        keystoreFileSelectButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                if (keystoreTypeComboBox.getValue().equals("JKS")) {
                    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("JKS 证书私钥文件", "*.jks"));
                } else {
                    fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("PKCS12 证书私钥文件", "*.pfx", "*.p12"));
                }
                File file = fileChooser.showOpenDialog(primaryStage);
                if (file != null) {
                    keystoreFileTextField.setText(file.getAbsolutePath());
                }
            }
        });
        gridPane.add(keystoreFileSelectButton, 2, 5);
        Label keystorePassLabel = new Label("证书密码：");
        gridPane.add(keystorePassLabel, 0, 6);
        final PasswordField keystorePassField = new PasswordField();
        gridPane.add(keystorePassField, 1, 6);

        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (webRootTextField.getText().length() == 0) {
                    webRootTextField.setText(".");
                }
                if (portTextField.getText().length() == 0) {
                    portTextField.setText("80");
                }
                if (sslPortTextField.getText().length() == 0) {
                    sslPortTextField.setText("443");
                }

                System.setProperty("webRoot", webRootTextField.getText());
                System.setProperty("port", portTextField.getText());
                System.setProperty("keystoreFile", keystoreFileTextField.getText());
                System.setProperty("keystorePass", keystorePassField.getText());
                System.setProperty("keystoreType", keystoreTypeComboBox.getValue());
                System.setProperty("sslProtocol", sslProtocolComboBox.getValue());
                System.setProperty("sslPort", sslPortTextField.getText());

                try {
                    startButton.setDisable(true);
                    httpServer = new HttpServer();
                    httpsServer = new HttpsServer();
                    httpServer.start();
                    httpsServer.start();
                    stopButton.setDisable(false);
                    errorLabel.setText("");
                } catch (Exception e) {
                    errorLabel.setText(e.toString());
                    startButton.setDisable(false);
                }
            }
        });
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                httpServer.interrupt();
                httpsServer.interrupt();
                stopButton.setDisable(true);
                startButton.setDisable(false);
            }
        });
        buttonBar.getButtons().addAll(startButton, stopButton);
        gridPane.add(buttonBar, 0, 7, 3, 1);
        gridPane.add(errorLabel, 0, 8, 3, 1);

        primaryStage.setResizable(false);
        primaryStage.setTitle("HTTP 服务");
        primaryStage.setScene(new Scene(gridPane));
        primaryStage.show();
    }

    private void setNumberField(final TextField sslPortTextField) {
        sslPortTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.length() != 0) {
                    try {
                        int value = Integer.parseInt(newValue);
                        if (value > 65535 || value < 0) {
                            sslPortTextField.setText(oldValue);
                        }
                    } catch (Exception e) {
                        sslPortTextField.setText(oldValue);
                    }
                }
            }
        });
    }
}
