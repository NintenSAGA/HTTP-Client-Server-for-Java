package util.packer.transencode;

import util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static util.consts.TransferEncoding.CONTENT_LENGTH;
import static util.consts.TransferEncoding.CHUNKED;
import static util.consts.TransferEncoding.TRANSFER_ENCODING;

public class ChunkedStrategy extends TransEncodeStrategy {
    private static final int CHUNK_SIZE = 500;     // chunk size in bytes
    private boolean done;

    @Override
    public byte[] readBytes() throws IOException {
        if (done) return new byte[0];

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] bytes;

        for (; (bytes =  upper.readNBytes(CHUNK_SIZE)).length != 0; ) {
            outputStream.write("%s\r\n"
                    .formatted(
                            Integer.toString(bytes.length, 16)
                    ).getBytes());
//            Log.debug("Writing...");
            outputStream.write(bytes);
            outputStream.write("\r\n".getBytes());
        }

        outputStream.write("0\r\n\r\n".getBytes());
        done = true;

//        if ((bytes =  upper.readNBytes(CHUNK_SIZE)).length != 0) {
//            outputStream.write("%s\r\n"
//                    .formatted(
//                            Integer.toString(bytes.length, 16)
//                    ).getBytes());
////            Log.debug("Writing...");
//            outputStream.write(bytes);
//            outputStream.write("\r\n".getBytes());
////            Log.debug("%d written".formatted(bytes.length));
//        } else if (!done) {
//            outputStream.write("0\r\n\r\n".getBytes());
//            done = true;
//        }

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
        assert !headers.containsKey(CONTENT_LENGTH);
        headers.merge(TRANSFER_ENCODING, CHUNKED, "%s, %s"::formatted);
        done = false;
    }
}
