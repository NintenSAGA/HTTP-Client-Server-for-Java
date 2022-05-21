package util.packer;

import util.Config;
import util.HttpMessage;
import util.Log;
import util.packer.encode.ContentGzipStrategy;
import util.packer.encode.TransChunkedStrategy;
import util.packer.encode.TransContentLengthStrategy;
import util.packer.encode.SourceStrategy;
import util.packer.encode.EncodeStrategy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.WritePendingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

import static util.consts.Headers.*;

public class MessagePacker {

    private final
    Map<String, EncodeStrategy> strategyMap;

    private final
    HttpMessage message;
    private final
    String[]    transferEncodings;

    private
    AsynchronousSocketChannel socket;
    private
    Queue<byte[]> byteArrayQueue;
    private
    String acceptEncoding;

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
        this(message, transferEncodings, "");
    }

    public MessagePacker(HttpMessage message, String[] transferEncodings, String acceptEncoding) {
        this.message = message;
        this.transferEncodings = transferEncodings;
        this.socket = null;
        this.acceptEncoding = Objects.requireNonNullElseGet(acceptEncoding, ()->"");

        strategyMap = new HashMap<>();
        strategyMap.put( CONTENT_LENGTH,    new TransContentLengthStrategy() );
        strategyMap.put( CHUNKED,           new TransChunkedStrategy() );
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

        InputStream bodyStream = message.getBodyAsStream();

        EncodeStrategy upperStrategy = new SourceStrategy(bodyStream);

        if (transferEncodings == null) {
            // -------------------- 1. Content Encoding -------------------- //
            /*  Only support gzip.                          */
            /*  Can't be used without Content-Length,       */
            /*  since length after encoding is unknown      */
            if (acceptEncoding.contains("gzip")
                && Integer.parseInt(
                        message.getHeaders().getOrDefault(CONTENT_LENGTH, "0")
                    ) >= Config.GZIP_THRESHOLD
            ) {
                Log.debug("Content encoded with gzip");
                EncodeStrategy gzipStrategy = new ContentGzipStrategy();
                upperStrategy = gzipStrategy.connect(message.getHeaders(), upperStrategy);
            }
        } else {
            // -------------------- 2. Transfer Encoding -------------------- //

            for (String te : transferEncodings) {
                if (!strategyMap.containsKey(te))
                    Log.panic("Unsupported transfer-encoding[%s]!".formatted(te));
                upperStrategy = strategyMap.get(te).connect(message.getHeaders(), upperStrategy);
            }
        }

        // -------------------- 3. Send out start line and headers -------------------- //
        written += flush(message.getStartLineAndHeadersAsStream());

        // -------------------- 4. Send out -------------------- //
        for (byte[] bytes; (bytes = upperStrategy.readBytes()).length != 0; ) {
            ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            written += flush(stream);
        }

        if (socket != null)
            Log.logSocket(socket,"Message sent %f KB ".formatted((double) written / (1 << 10)));

        return written;
    }

    private int flush(InputStream stream)
            throws ExecutionException, InterruptedException, IOException {
        int written = 0;

        if (socket != null) {
            for (byte[] bytes; (bytes = stream.readNBytes(Config.SOCKET_BUFFER_SIZE)).length != 0; ) {
                try {
                    var future = socket.write(
                            ByteBuffer.wrap(bytes)
                    );
                    int delta = future.get();
                    assert Log.testExpect("Bytes sent", bytes.length, delta);
                    written += delta;
                } catch (WritePendingException e) {
                    e.printStackTrace();
                }
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
