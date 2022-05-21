package util.parser.contentdecode;

import util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class GzipStrategy implements ContentDecodeStrategy {

    @Override
    public byte[] getBody(Map<String, String> headers, byte[] bytes) {
        assert bytes != null;
        assert bytes.length != 0;
        Log.debug("Body was unzipped with GZIP");
        try (GZIPInputStream gzipIn = new GZIPInputStream(
                new ByteArrayInputStream(bytes)
            )
        ) {
            return gzipIn.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
