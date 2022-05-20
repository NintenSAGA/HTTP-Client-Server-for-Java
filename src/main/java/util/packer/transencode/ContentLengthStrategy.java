package util.packer.transencode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static util.consts.TransferEncoding.CONTENT_LENGTH;

public class ContentLengthStrategy extends TransEncodeStrategy {
    private byte[] bytes;

    @Override
    protected void headerEditing() throws IOException {
        bytes = inputStream.readAllBytes();
        int length = bytes.length;
        headers.put(CONTENT_LENGTH, String.valueOf(length));
    }

    @Override
    protected void encode() throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
}
