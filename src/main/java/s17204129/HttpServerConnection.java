package s17204129;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public abstract class HttpServerConnection implements HttpConnection, Runnable {

    private final HttpRequestParser requestParser;

    private final Socket socket;

    public HttpServerConnection(Socket socket) throws IOException {
        this.socket = socket;
        requestParser = new HttpRequestParser(socket.getInputStream());
    }

    protected abstract void handleRequest(HttpRequestHeader requestHeader) throws IOException;

    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    protected void append(byte[] data) {
        byteArrayOutputStream.write(data, 0, data.length);
    }

    protected void append(String data) {
        final byte[] bytes = (data + "\r\n").getBytes();
        byteArrayOutputStream.write(bytes, 0, bytes.length);
    }

    protected void send(HttpResponseHeader responseHeader) throws IOException {
        responseHeader.getHeaders().put(SupportedHttpHeaders.CONTENT_LENGTH, Integer.toString(byteArrayOutputStream.size()));
        responseHeader.getHeaders().put(SupportedHttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");
        getOutputStream().write(responseHeader.toString().getBytes(StandardCharsets.US_ASCII));
        getOutputStream().write(byteArrayOutputStream.toByteArray());
        getOutputStream().flush();
        byteArrayOutputStream = new ByteArrayOutputStream();
    }

    @Override
    public void run() {
        try {
            HttpRequestHeader requestHeader = requestParser.parse();
            handleRequest(requestHeader);
            getOutputStream().flush();
            close();
        } catch (Exception e) {
            try {
                HttpResponseHeader responseHeader = new HttpResponseHeader();
                responseHeader.setStatusCode(SupportedHttpStatusCodes.INTERNAL_SERVER_ERROR);
                responseHeader.setStatusDescription(SupportedHttpStatusDescriptions.INTERNAL_SERVER_ERROR);
                append(e.toString());
                send(responseHeader);
                close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public InputStream getInputStream() {
        return requestParser.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
