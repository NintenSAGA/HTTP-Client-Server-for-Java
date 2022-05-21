package util.packer.transencode;

import util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static util.consts.TransferEncoding.CONTENT_LENGTH;

public class ContentLengthStrategy extends TransEncodeStrategy {
    private ByteArrayInputStream bis;
    private boolean done;

    @Override
    public byte[] readBytes() {
        if (done) return new byte[0];
        done = true;
        return bis.readAllBytes();
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
        headers.put(CONTENT_LENGTH, String.valueOf(length));
        Log.debug("Content_length: %d".formatted(length));
        done = false;
    }
}
