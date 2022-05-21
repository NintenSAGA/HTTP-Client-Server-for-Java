package util.packer.encode;

import util.Log;
import util.consts.Headers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class ContentGzipStrategy extends EncodeStrategy {
    ByteArrayInputStream inputStream;

    private void prepare() throws IOException {
        if (inputStream != null) return;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(
                    outputStream
            )) {
                byte[] bytes = upper.readBytes();
                gzipOutputStream.write(bytes, 0, bytes.length);
                gzipOutputStream.finish();
            }
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        prepare();
        return inputStream.readAllBytes();
    }

    @Override
    protected byte[] readNBytes(int n) throws IOException {
        prepare();
        return inputStream.readNBytes(n);
    }

    @Override
    protected void headerEditing() throws IOException {
        inputStream = null;
        headers.put(Headers.CONTENT_ENCODING, Headers.GZIP);
    }
}
