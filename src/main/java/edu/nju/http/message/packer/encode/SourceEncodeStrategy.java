package edu.nju.http.message.packer.encode;

import edu.nju.http.util.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SourceEncodeStrategy extends EncodeStrategy {
    private final
    InputStream inputStream;

    private SourceEncodeStrategy(){
        this.inputStream = new ByteArrayInputStream(new byte[0]);
    }

    public SourceEncodeStrategy(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return inputStream.readNBytes(Config.SOCKET_BUFFER_SIZE);
    }

    @Override
    protected byte[] readNBytes(int n) throws IOException {
        return inputStream.readNBytes(n);
    }

    @Override
    protected void headerEditing() throws IOException {

    }
}
