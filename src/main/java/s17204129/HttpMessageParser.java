package s17204129;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public abstract class HttpMessageParser {

    private static final Pattern HEADER_LINE_PATTERN = Pattern.compile("^(?<name>.+):( (?<value>.+))?$");

    private static final Pattern HTTP_VERSION_PATTERN = Pattern.compile("HTTP/(?<httpMajorVersion>\\d+)\\.(?<httpMinorVersion>\\d+)");

    private final InputStream inputStream;

    public HttpMessageParser(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    protected String readLine() throws IOException {
        final byte CR = '\r', LF = '\n';
        final StringBuilder builder = new StringBuilder();
        loop:
        for (;;) {
            int read = inputStream.read();
            switch (read) {
                case -1:
                case LF: {
                    break loop;
                }
                case CR: {
                    break;
                }
                default: {
                    builder.append((char)read);
                    break;
                }
            }
        }
        return builder.toString();
    }

    protected Map<String, String> parseHeaders() throws HttpMessageParseException {
        HashMap<String, String> headers = new HashMap<>();
        try {
            for (; ; ) {
                String headerLine = readLine();
                if (headerLine.length() == 0) {
                    break;
                }
                Matcher matcher = HEADER_LINE_PATTERN.matcher(headerLine);
                if (matcher.find()) {
                    String name = matcher.group("name");
                    String value = matcher.group("value");
                    headers.put(name, value);
                } else {
                    throw new HttpMessageParseException(String.format("Invalid header: \"%s\"", headerLine));
                }
            }
            return headers;
        }
        catch (IOException e) {
            throw new HttpMessageParseException(e);
        }
    }

    protected HttpVersion parseHttpVersion(String sequence) throws HttpMessageParseException {
        HttpVersion version = new HttpVersion();
        Matcher matcher = HTTP_VERSION_PATTERN.matcher(sequence);
        if (matcher.find()) {
            int httpMajorVersion = Integer.parseInt(matcher.group("httpMajorVersion"));
            int httpMinorVersion = Integer.parseInt(matcher.group("httpMinorVersion"));
            version.setMajorVersion(httpMajorVersion);
            version.setMinorVersion(httpMinorVersion);
            return version;
        }
        else {
            throw new HttpMessageParseException(String.format("Invalid HTTP version: \"%s\"", sequence));
        }
    }

    public abstract HttpMessageHeader parse() throws HttpMessageParseException;

    public InputStream getInputStream() {
        return inputStream;
    }
}
