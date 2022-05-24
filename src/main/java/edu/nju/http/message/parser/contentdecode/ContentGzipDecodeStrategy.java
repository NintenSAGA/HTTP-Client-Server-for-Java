package edu.nju.http.message.parser.contentdecode;

import edu.nju.http.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ContentGzipDecodeStrategy implements ContentDecodeStrategy {

    @Override
    public byte[] getBody(Map<String, String> headers, byte[] bytes) {
        assert bytes != null;
        assert bytes.length != 0;
        Log.debug("Body was unzipped with GZIP");
        try (GZIPInputStream gzipIn = new GZIPInputStream(
                new ByteArrayInputStream(bytes)
            )
        ) {
            byte[] b = gzipIn.readAllBytes();
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
