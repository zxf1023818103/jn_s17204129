package s17204129;

public class HttpMessageParseException extends Exception {

    public HttpMessageParseException() {
        super();
    }

    public HttpMessageParseException(String message) {
        super(message);
    }

    public HttpMessageParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpMessageParseException(Throwable cause) {
        super(cause);
    }

    protected HttpMessageParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
