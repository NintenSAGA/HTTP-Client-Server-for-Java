package util.packer;

import util.HttpMessage;
import util.Log;
import util.packer.transencode.ChunkedStrategy;
import util.packer.transencode.ContentLengthStrategy;
import util.packer.transencode.TransEncodeStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static util.consts.TransferEncoding.*;

public class MessagePacker {
    private final static
    int TRANSPORT_SLICE_SIZE = 1 << 10;

    private final static
    Map<String, TransEncodeStrategy> strategyMap;
    static {
        strategyMap = new HashMap<>();
        strategyMap.put( CONTENT_LENGTH,    new ContentLengthStrategy() );
        strategyMap.put( CHUNKED,           new ChunkedStrategy() );
    }

    private final
    HttpMessage message;
    private final
    String[]    transferEncodings;

    private
    AsynchronousSocketChannel socket;
    private
    Queue<byte[]> byteArrayQueue;

    // ==================== Constructors ==================== //

    /**
     * Use default encoding with content-length
     */
    public MessagePacker(HttpMessage message) {
        this(message, null);
    }

    /**
     * Use designated encoding
     */
    public MessagePacker(HttpMessage message, String[] transferEncodings) {
        this.message = message;
        this.transferEncodings = transferEncodings;
        this.socket = null;
    }

    // ==================== Public ==================== //

    /**
     * Send out the message
     * @param socket socket channel
     * @return bytes written
     */
    public int send(AsynchronousSocketChannel socket)
            throws ExecutionException, InterruptedException, IOException {
        this.socket = socket;
        return send();
    }

    /**
     * Export to byte array. Used for testing.
     */
    public byte[] toByteArray()
            throws IOException, ExecutionException, InterruptedException {
        byteArrayQueue = new LinkedList<>();
        send();
        int sumLen = byteArrayQueue.stream()
                .mapToInt(a -> a.length)
                .sum();
        ByteBuffer ret = ByteBuffer.allocate(sumLen);
        while (!byteArrayQueue.isEmpty())
            ret.put(byteArrayQueue.poll());
        return ret.array();
    }

    // ==================== Private ==================== //

    private int send()
            throws IOException, ExecutionException, InterruptedException {
        int written = 0;

        // -------------------- 1. Transfer Encoding -------------------- //
        InputStream bodyStream = message.getBodyAsStream();

        //*               1. Content-Length               */
        if (transferEncodings == null) {
            bodyStream = strategyMap.get(CONTENT_LENGTH).encode(message.getHeaders(), bodyStream);
        } else {
            for (String te : transferEncodings) {
                if (!strategyMap.containsKey(te))
                    Log.panic("Unsupported transfer-encoding[%s]!".formatted(te));
                bodyStream = strategyMap.get(te).encode(message.getHeaders(), bodyStream);
            }
        }

        // -------------------- 2. Send out start line and headers -------------------- //
        written += flush(message.getStartLineAndHeadersAsStream());

        // -------------------- 3. Send out -------------------- //
        written += flush(bodyStream);

        Log.logSocket(socket,"Response sent %f KB ".formatted((double) written / (1 << 10)));

        return written;
    }

    private int flush(InputStream stream)
            throws ExecutionException, InterruptedException, IOException {
        int written = 0;

        if (socket != null) {
            for (byte[] bytes; (bytes = stream.readNBytes(TRANSPORT_SLICE_SIZE)).length != 0; ) {
                var future = socket.write(
                        ByteBuffer.wrap(bytes)
                );
                int delta = future.get();
                Log.testExpect("Bytes sent", bytes.length, delta);
                written += delta;
            }
        } else {
            assert byteArrayQueue != null;
            byte[] bytes = stream.readAllBytes();
            byteArrayQueue.offer(bytes);
            written = bytes.length;
        }

        return written;
    }

}
