package edu.nju.http.message.packer.encode;

import edu.nju.http.message.consts.Headers;
import edu.nju.http.util.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class ContentGzipEncodeStrategy extends EncodeStrategy {
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
    public byte[] readAllBytes() throws IOException {
        prepare();
        return inputStream.readAllBytes();
    }

    @Override
    public byte[] readBytes() throws IOException {
        return inputStream.readNBytes(Config.GZIP_MAXSIZE);
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
