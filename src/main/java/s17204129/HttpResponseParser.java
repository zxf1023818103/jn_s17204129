package s17204129;

import java.io.*;
import java.util.regex.*;

public class HttpResponseParser extends HttpMessageParser {

    private static final Pattern STATUS_LINE_PATTERN = Pattern.compile("^(?<httpVersion>.+) (?<statusCode>\\d+) (?<statusDescription>.+)$");

    public HttpResponseParser(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public HttpResponseHeader parse() throws HttpMessageParseException {
        try {
            String statusLine = readLine();
            Matcher matcher = STATUS_LINE_PATTERN.matcher(statusLine);
            if (matcher.find()) {
                HttpResponseHeader responseHeader = new HttpResponseHeader();
                int statusCode = Integer.parseInt(matcher.group("statusCode"));
                String statusDescription = matcher.group("statusDescription");
                responseHeader.setHttpVersion(parseHttpVersion(matcher.group("httpVersion")));
                responseHeader.setStatusCode(statusCode);
                responseHeader.setStatusDescription(statusDescription);
                responseHeader.getHeaders().putAll(parseHeaders());
                return responseHeader;
            }
            else {
                throw new HttpMessageParseException(String.format("Invalid status line: \"%s\"", statusLine));
            }
        }
        catch (IOException e){
            throw new HttpMessageParseException(e);
        }
    }
}
