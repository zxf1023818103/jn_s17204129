package s17204129;

import java.io.*;

public interface HttpConnection extends Closeable {

    InputStream getInputStream() throws IOException;

    OutputStream getOutputStream() throws IOException;
}
