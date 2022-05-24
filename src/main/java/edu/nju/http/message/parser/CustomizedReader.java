package edu.nju.http.message.parser;

import edu.nju.http.exception.InvalidMessageException;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CustomizedReader {
    private final static
    int TEMP_CAP = 1 << 20;

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

    public CustomizedReader(AsynchronousSocketChannel socket, ByteBuffer byteBuffer, int timeout) {
        this.socket = socket;
        this.byteBuffer = byteBuffer;
        this.timeout = timeout;
        this.tempBuffer = ByteBuffer.allocate(TEMP_CAP);
    }

    public CustomizedReader(byte[] byteArray) {
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

        assert tempBuffer.limit() >= n;

        for (int i = 0, b; i < n && (b = read()) != -1; i++) {
            tempBuffer.put((byte) b);
        }

        return tempBufferPop();
    }

    // -------------------- private -------------------- //

    private byte[] bufferPop(ByteBuffer buffer) {
        buffer.flip();
        byte[] ret = new byte[buffer.limit()];
        buffer.get(ret);
        buffer.clear();

        return ret;
    }

    private byte[] tempBufferPop() {
        return bufferPop(tempBuffer);
    }

    private void reload()
            throws ExecutionException, InterruptedException,
            TimeoutException, InvalidMessageException {

        if (socket != null
                && (byteInStream == null || byteInStream.available() == 0) ) {

            var future = socket.read(byteBuffer);
            int count = future.get(timeout, TimeUnit.MILLISECONDS);
            if (count == -1)
                throw new TimeoutException();
//            Log.debug("Reload");
            byteInStream = new ByteArrayInputStream(bufferPop(byteBuffer));
        }
    }

    private int read()
            throws ExecutionException, InterruptedException, TimeoutException, InvalidMessageException {
        reload();
        return this.byteInStream.read();
    }


}
