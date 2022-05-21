package util.packer.transencode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SourceStrategy extends TransEncodeStrategy {
    private final
    InputStream inputStream;

    private SourceStrategy(){
        inputStream = new ByteArrayInputStream(new byte[0]);
    }

    public SourceStrategy(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public byte[] readBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    protected byte[] readNBytes(int n) throws IOException {
        return inputStream.readNBytes(n);
    }

    @Override
    protected void headerEditing() throws IOException {

    }
}
