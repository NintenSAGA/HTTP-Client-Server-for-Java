package edu.nju.http.message.packer.encode;

import edu.nju.http.util.Config;
import edu.nju.http.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static edu.nju.http.message.consts.Headers.CONTENT_LENGTH;

@Deprecated
public class TransContentLengthEncodeStrategy extends EncodeStrategy {
    private ByteArrayInputStream bis;

    @Override
    public byte[] readAllBytes() throws IOException {
        if (bis.available() == 0) return new byte[0];
        return bis.readAllBytes();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return readNBytes(Config.SOCKET_BUFFER_SIZE);
    }

    @Override
    protected byte[] readNBytes(int n) throws IOException {
        return bis.readNBytes(n);
    }

    @Override
    protected void headerEditing() throws IOException {

        byte[] bytes = upper.readBytes();
        int length = bytes.length;
        bis = new ByteArrayInputStream(bytes);
        if (length == 0) return;

        headers.put(CONTENT_LENGTH, String.valueOf(length));
        Log.debug("Content_length: %d".formatted(length));
    }
}
