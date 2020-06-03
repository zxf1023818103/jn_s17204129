package s17204129;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class HttpRequestParser extends HttpMessageParser {

    private static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^(?<method>.+)\\s(?<requestUri>.+)\\s(?<httpVersion>.+)$");

    public HttpRequestParser(InputStream input) {
        super(input);
    }

    @Override
    public HttpRequestHeader parse() throws HttpMessageParseException {
        try {
            for (; ; ) {
                String requestLine = readLine();
                if (requestLine.length() != 0) {
                    Matcher matcher = REQUEST_LINE_PATTERN.matcher(requestLine);
                    if (matcher.find()) {
                        HttpRequestHeader requestHeader = new HttpRequestHeader();
                        String method = matcher.group("method");
                        String requestUri = matcher.group("requestUri");
                        requestHeader.setMethod(method);
                        try {
                            requestHeader.setRequestUri(URI.create(requestUri));
                            requestHeader.setHttpVersion(parseHttpVersion(matcher.group("httpVersion")));
                            requestHeader.getHeaders().putAll(parseHeaders());
                            return requestHeader;
                        } catch (IllegalArgumentException e) {
                            throw new HttpMessageParseException(String.format("Invalid HTTP request URI: \"%s\"", requestUri));
                        }
                    } else {
                        throw new HttpMessageParseException("Invalid HTTP request line");
                    }
                }
            }
        }
        catch (IOException e) {
            throw new HttpMessageParseException(e);
        }
    }
}
