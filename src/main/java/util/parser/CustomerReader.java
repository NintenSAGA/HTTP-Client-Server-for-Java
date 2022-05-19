package util.parser;

import exception.InvalidMessageException;
import util.Log;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CustomerReader {
    private final static
    int TEMP_CAP = 1 << 10;

    private final
    AsynchronousSocketChannel socket;
    private final
    ByteBuffer byteBuffer;
    private final
    int timeout;
    private final
    ByteBuffer tempBuffer;

    private
    ByteArrayInputStream byteInStream;

    // -------------------- public -------------------- //

    public CustomerReader(AsynchronousSocketChannel socket, ByteBuffer byteBuffer, int timeout) {
        this.socket = socket;
        this.byteBuffer = byteBuffer;
        this.timeout = timeout;
        this.tempBuffer = ByteBuffer.allocate(TEMP_CAP);
    }

    public CustomerReader(byte[] byteArray) {
        this.socket = null;
        this.byteBuffer = null;
        this.timeout = 0;
        this.tempBuffer = ByteBuffer.allocate(TEMP_CAP);

        this.byteInStream = new ByteArrayInputStream(byteArray);
    }

    public String readLine()
            throws InvalidMessageException, ExecutionException,
            InterruptedException, TimeoutException {

        for (int b; (b = read()) != -1 && b != '\r'; )
            tempBuffer.put((byte) b);

        // HTTP/1.1 defines the sequence CR LF as the end-of-line marker for all
        // protocol elements except the entity-body
        if (read() != '\n')
            throw new InvalidMessageException();

        return new String(tempBufferPop());
    }

    public byte[] readNBytes(int n)
            throws InvalidMessageException, ExecutionException,
            InterruptedException, TimeoutException {

        for (int i = 0, b; i < n && (b = read()) != -1; i++) {
            tempBuffer.put((byte) b);
        }

        return tempBufferPop();
    }

    // -------------------- private -------------------- //

    private byte[] tempBufferPop() {
        tempBuffer.flip();
        byte[] ret = new byte[tempBuffer.limit()];
        tempBuffer.get(ret);
        tempBuffer.clear();

        return ret;
    }

    private void reload()
            throws ExecutionException, InterruptedException,
            TimeoutException, InvalidMessageException {

        if (socket != null
                && (byteInStream == null || byteInStream.available() == 0) ) {
            byteBuffer.clear();
            var future = socket.read(byteBuffer);
            int count = future.get(timeout, TimeUnit.MILLISECONDS);
            if (count == -1)
                throw new InvalidMessageException();
//            Log.debug("Reload");
            byteInStream = new ByteArrayInputStream(byteBuffer.array());
        }
    }

    private int read()
            throws ExecutionException, InterruptedException, TimeoutException, InvalidMessageException {
        reload();
        return this.byteInStream.read();
    }


}
