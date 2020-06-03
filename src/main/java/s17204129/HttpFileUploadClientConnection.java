package s17204129;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class HttpFileUploadClientConnection implements HttpConnection {

    protected abstract void onProgress(int bytesThisTransfer, int bytesTransferred, int bytesTotal);

    public HttpResponseHeader sendRequest(HttpRequestHeader requestHeader, FileInputStream bodyStream, int bytesTotal) throws IOException, HttpMessageParseException {
        getOutputStream().write(requestHeader.toString().getBytes(StandardCharsets.US_ASCII));
        if (bodyStream != null) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesTransferred = 0;
            while (bytesTransferred < bytesTotal) {
                int bytesThisTransfer = bodyStream.read(buffer, 0, Math.min(buffer.length, bytesTotal - bytesTransferred));
                getOutputStream().write(buffer, 0, bytesThisTransfer);
                bytesTransferred += bytesThisTransfer;
                onProgress(bytesThisTransfer, bytesTransferred, bytesTotal);
            }
        }
        getOutputStream().flush();
        return new HttpResponseParser(getInputStream()).parse();
    }
}
