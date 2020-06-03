package s17204129;

import java.util.HashMap;
import java.util.Map;

public abstract class HttpMessageHeader {

    private HttpVersion httpVersion = new HttpVersion();

    private final Map<String, String> headers = new HashMap<>();

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    protected static void appendHeaders(StringBuilder builder, Map<String, String> headers) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey());
            String value = entry.getValue();
            builder.append(':');
            if (value != null && !value.isEmpty()) {
                builder.append(' ');
                builder.append(value);
            }
            builder.append("\r\n");
        }
    }
}
