package s17204129;

public class HttpVersion {

    private int majorVersion = 1;

    private int minorVersion = 1;

    public HttpVersion() {
    }

    public HttpVersion(int majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public String toString() {
        return "HTTP/" + majorVersion + "." + minorVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpVersion version = (HttpVersion) o;

        if (majorVersion != version.majorVersion) return false;
        return minorVersion == version.minorVersion;
    }

    @Override
    public int hashCode() {
        int result = majorVersion;
        result = 31 * result + minorVersion;
        return result;
    }
}
