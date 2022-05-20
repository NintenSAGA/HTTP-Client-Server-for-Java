package util.packer.transencode;

import util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static util.consts.TransferEncoding.CONTENT_LENGTH;
import static util.consts.TransferEncoding.CHUNKED;
import static util.consts.TransferEncoding.TRANSFER_ENCODING;

public class ChunkedStrategy extends TransEncodeStrategy {
    private static final int CHUNK_SIZE = 500;     // chunk size in char

    @Override
    protected void headerEditing() throws IOException {
        assert !headers.containsKey(CONTENT_LENGTH);
        headers.merge(TRANSFER_ENCODING, CHUNKED, "%s, %s"::formatted);
    }

    @Override
    protected void encode() throws IOException {
        while (inputStream.available() != 0) {
            Log.debug("Reading... [%d]".formatted(inputStream.available()));
            byte[] bytes =  inputStream.readNBytes(CHUNK_SIZE);
            outputStream.write("%s\r\n"
                    .formatted(
                            Integer.toString(bytes.length, 16)
                    ).getBytes());
            Log.debug("Writing...");
            outputStream.write(bytes);
            outputStream.write("\r\n".getBytes());
            outputStream.flush();
            Log.debug("%d written".formatted(bytes.length));
        }
        outputStream.write("0\r\n\r\n".getBytes());
        outputStream.close();
        Log.debug("Finished");

    }
}
