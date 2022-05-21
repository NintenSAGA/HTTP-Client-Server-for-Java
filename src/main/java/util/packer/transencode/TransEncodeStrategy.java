package util.packer.transencode;

import java.io.IOException;
import java.util.Map;

public abstract class TransEncodeStrategy {
    protected TransEncodeStrategy upper;
    protected Map<String, String> headers;

    public TransEncodeStrategy connect(Map<String, String> headers, TransEncodeStrategy upper)
            throws IOException {
        this.headers = headers;
        this.upper = upper;

        headerEditing();

        return this;
    }

    public abstract byte[] readBytes() throws IOException;

    protected abstract byte[] readNBytes(int n) throws IOException;

    protected abstract void headerEditing() throws IOException;

}
