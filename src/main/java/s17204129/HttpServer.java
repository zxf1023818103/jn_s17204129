package s17204129;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;


public class HttpServer extends Thread {

    private ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            if (serverSocket == null) {
                int port = Integer.parseInt(System.getProperty("port"));
                serverSocket = new ServerSocket(port);
            }
            for (; ; ) {
                try {
                    final Socket socket = serverSocket.accept();
                    new HttpServerConnection(socket) {
                        @Override
                        protected void handleRequest(HttpRequestHeader requestHeader) throws IOException {
                            HttpResponseHeader responseHeader = new HttpResponseHeader();
                            if (requestHeader.getHttpVersion().equals(SupportedHttpVersion.HTTP_1_1)) {
                                String path = requestHeader.getRequestUri().getPath();
                                String webRootDirectory = System.getProperty("webRoot", ".");
                                Path filePath = Paths.get(webRootDirectory, path);
                                switch (requestHeader.getMethod()) {
                                    case SupportedHttpMethods.GET: {
                                        try {
                                            File file = filePath.toFile();
                                            if (file.isDirectory()) {
                                                String[] filenames = file.list();
                                                if (filenames != null) {
                                                    for (String filename : filenames) {
                                                        append((filename + "\r\n").getBytes(StandardCharsets.UTF_8));
                                                    }
                                                }
                                                append("\r\n".getBytes(StandardCharsets.UTF_8));
                                                send(responseHeader);
                                            } else {
                                                FileInputStream inputStream = new FileInputStream(file);
                                                responseHeader.setStatusCode(SupportedHttpStatusCodes.OK);
                                                responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.OK);
                                                responseHeader.getHeaders().put(SupportedHttpHeaders.CONTENT_LENGTH, Integer.toString(inputStream.available()));
                                                responseHeader.getHeaders().put(SupportedHttpHeaders.CONTENT_TYPE, "application/force-download");
                                                byte[] buffer = new byte[1024 * 1024];
                                                int bytesRead;
                                                getOutputStream().write(responseHeader.toString().getBytes(StandardCharsets.US_ASCII));
                                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                                    getOutputStream().write(buffer, 0, bytesRead);
                                                }
                                            }
                                        } catch (FileNotFoundException e) {
                                            responseHeader.setStatusCode(SupportedHttpStatusCodes.NOT_FOUND);
                                            responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.NOT_FOUND);
                                            append(e.toString());
                                            send(responseHeader);
                                        } catch (Exception e) {
                                            responseHeader.setStatusCode(SupportedHttpStatusCodes.INTERNAL_SERVER_ERROR);
                                            responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.INTERNAL_SERVER_ERROR);
                                            append(e.toString());
                                            send(responseHeader);
                                        }
                                        break;
                                    }
                                    case SupportedHttpMethods.PUT: {
                                        try {
                                            String contentLengthString = requestHeader.getHeaders().get(SupportedHttpHeaders.CONTENT_LENGTH);
                                            if (contentLengthString != null) {
                                                int contentLength = Integer.parseInt(contentLengthString);
                                                FileOutputStream outputStream = new FileOutputStream(filePath.toFile());
                                                InputStream inputStream = getInputStream();
                                                byte[] buffer = new byte[1024 * 1024];
                                                int bytesTotal = 0;
                                                while (bytesTotal < contentLength) {
                                                    int bytesRead = inputStream.read(buffer, 0, Math.min(buffer.length, contentLength - bytesTotal));
                                                    if (bytesRead == -1) {
                                                        responseHeader.setStatusCode(SupportedHttpStatusCodes.BAD_REQUEST);
                                                        responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.BAD_REQUEST);
                                                        getOutputStream().write(responseHeader.toString().getBytes(StandardCharsets.US_ASCII));
                                                        break;
                                                    }
                                                    outputStream.write(buffer, 0, bytesRead);
                                                    bytesTotal += bytesRead;
                                                }
                                                outputStream.close();
                                                responseHeader.setStatusCode(SupportedHttpStatusCodes.CREATED);
                                                responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.CREATED);
                                                append(SupportedHttpStatusDescriptions.CREATED);
                                                send(responseHeader);
                                            } else {
                                                throw new NumberFormatException();
                                            }
                                        } catch (FileNotFoundException e) {
                                            responseHeader.setStatusCode(SupportedHttpStatusCodes.NOT_FOUND);
                                            responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.NOT_FOUND);
                                            append(e.toString());
                                            send(responseHeader);
                                        } catch (NumberFormatException e) {
                                            responseHeader.setStatusCode(SupportedHttpStatusCodes.LENGTH_REQUIRED);
                                            responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.LENGTH_REQUIRED);
                                            append(SupportedHttpStatusDescriptions.LENGTH_REQUIRED);
                                            send(responseHeader);
                                        } catch (Exception e) {
                                            responseHeader.setStatusCode(SupportedHttpStatusCodes.INTERNAL_SERVER_ERROR);
                                            responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.INTERNAL_SERVER_ERROR);
                                            append(e.toString());
                                            send(responseHeader);
                                        }
                                        break;
                                    }
                                }
                            } else {
                                responseHeader.setStatusCode(SupportedHttpStatusCodes.HTTP_VERSION_NOT_SUPPORTED);
                                responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.HTTP_VERSION_NOT_SUPPORTED);
                                append(SupportedHttpStatusDescriptions.HTTP_VERSION_NOT_SUPPORTED);
                                send(responseHeader);
                            }
                        }
                    }.run();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) {
        new HttpServer().start();
    }
}
