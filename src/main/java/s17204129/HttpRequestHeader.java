package s17204129;

import java.net.*;

public class HttpRequestHeader extends HttpMessageHeader {

    private String method = SupportedHttpMethods.GET;

    private URI requestUri;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URI getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(URI requestUri) {
        this.requestUri = requestUri;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(method)
                .append(' ')
                .append(requestUri.toASCIIString())
                .append(' ')
                .append(getHttpVersion())
                .append("\r\n");
        appendHeaders(builder, getHeaders());
        builder.append("\r\n");
        return builder.toString();
    }
}
