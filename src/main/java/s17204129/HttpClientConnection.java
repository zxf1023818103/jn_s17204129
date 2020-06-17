package s17204129;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class HttpClientConnection implements HttpConnection {

    private final Socket socket;
    private final AtomicInteger atomicBytesThisTransfer = new AtomicInteger(0), atomicBytesTransferred = new AtomicInteger(0), atomicBytesTotal = new AtomicInteger(0);

    protected abstract void onProgress(int bytesThisTransfer, int bytesTransferred, int bytesTotal);

    public HttpClientConnection(Socket socket) {
        this.socket = socket;
    }

    public HttpResponseHeader sendRequest(HttpRequestHeader requestHeader, FileInputStream bodyStream, final int bytesTotal) throws IOException, HttpMessageParseException {
        getOutputStream().write(requestHeader.toString().getBytes(StandardCharsets.US_ASCII));
        if (bodyStream != null) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesTransferred = 0;
            atomicBytesThisTransfer.set(0);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    onProgress(atomicBytesThisTransfer.get(), atomicBytesTransferred.get(), atomicBytesTotal.get());
                }
            }, 1000, 1000);
            while (bytesTransferred < bytesTotal) {
                int bytesThisTransfer = bodyStream.read(buffer);
                if (bytesThisTransfer == -1) {
                    break;
                }
                getOutputStream().write(buffer, 0, bytesThisTransfer);
                bytesTransferred += bytesThisTransfer;
                atomicBytesThisTransfer.set(bytesThisTransfer);
                atomicBytesTransferred.set(bytesTransferred);
                atomicBytesTotal.set(bytesTotal);
            }
            timer.cancel();
            onProgress(atomicBytesThisTransfer.get(), atomicBytesTransferred.get(), atomicBytesTotal.get());
        }
        getOutputStream().flush();
        return new HttpResponseParser(getInputStream()).parse();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
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
