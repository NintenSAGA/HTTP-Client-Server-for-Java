package message.packer.encode;

import java.io.IOException;
import java.util.Map;

public abstract class EncodeStrategy {
    protected EncodeStrategy upper;
    protected Map<String, String> headers;

    public EncodeStrategy connect(Map<String, String> headers, EncodeStrategy upper)
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
