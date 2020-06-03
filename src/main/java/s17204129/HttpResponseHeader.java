package s17204129;

public class HttpResponseHeader extends HttpMessageHeader {

    private int statusCode = SupportedHttpStatusCodes.OK;

    private String statusDescription = SupportedHttpStatusDescriptions.OK;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getHttpVersion())
                .append(' ')
                .append(statusCode)
                .append(' ')
                .append(statusDescription)
                .append("\r\n");
        appendHeaders(builder, getHeaders());
        builder.append("\r\n");
        return builder.toString();
    }
}
