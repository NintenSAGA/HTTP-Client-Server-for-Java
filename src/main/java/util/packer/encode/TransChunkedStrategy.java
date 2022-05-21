package util.packer.encode;

import util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static util.consts.Headers.CONTENT_LENGTH;
import static util.consts.Headers.CHUNKED;
import static util.consts.Headers.TRANSFER_ENCODING;

public class TransChunkedStrategy extends EncodeStrategy {
    private static final int CHUNK_SIZE = 800;     // chunk size in bytes
    private boolean done;

    @Override
    public byte[] readBytes() throws IOException {
        if (done) return new byte[0];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes;

        if ((bytes =  upper.readNBytes(CHUNK_SIZE)).length != 0) {
            outputStream.write("%s\r\n"
                    .formatted(
                            Integer.toString(bytes.length, 16)
                    ).getBytes());
//            Log.debug("Writing...");
            outputStream.write(bytes);
            outputStream.write("\r\n".getBytes());
//            Log.debug("%d written".formatted(bytes.length));
        } else if (!done) {
            outputStream.write("0\r\n\r\n".getBytes());
            done = true;
        }

//        Log.debug("Finished");

        return outputStream.toByteArray();
    }

    @Override
    @Deprecated
    protected byte[] readNBytes(int n) throws IOException {
        Log.panic("prohibit!");
        return null;
    }

    @Override
    protected void headerEditing() throws IOException {
        headers.remove(CONTENT_LENGTH);
        headers.merge(TRANSFER_ENCODING, CHUNKED, "%s, %s"::formatted);
        done = false;
    }
}
