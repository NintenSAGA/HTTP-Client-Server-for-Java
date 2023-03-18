package edu.nju.http.message.packer;

import edu.nju.http.message.HttpMessage;
import edu.nju.http.message.packer.encode.ContentGzipEncodeStrategy;
import edu.nju.http.message.packer.encode.EncodeStrategy;
import edu.nju.http.message.packer.encode.SourceEncodeStrategy;
import edu.nju.http.message.packer.encode.TransChunkedEncodeStrategy;
import edu.nju.http.util.Config;
import edu.nju.http.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.WritePendingException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static edu.nju.http.message.consts.Headers.*;

public class MessagePacker {

  private final
  Map<String, EncodeStrategy> strategyMap;

  private final
  HttpMessage message;
  private final
  String[] transferEncodings;
  private final
  String acceptEncoding;

  private final
  ByteBuffer bb;

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
    this(message, transferEncodings, "");
  }

  public MessagePacker(HttpMessage message, String[] transferEncodings, String acceptEncoding) {
    this.message = message;
    this.transferEncodings = transferEncodings;
    this.socket = null;
    this.acceptEncoding = Objects.requireNonNullElseGet(acceptEncoding, () -> "");
    this.bb = ByteBuffer.allocate(Config.SOCKET_BUFFER_SIZE);

    strategyMap = new HashMap<>();
    strategyMap.put(CHUNKED, new TransChunkedEncodeStrategy());
  }


  // ==================== Public ==================== //

  /**
   * Send out the message
   *
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

    EncodeStrategy upperStrategy = new SourceEncodeStrategy(bodyStream);

    int length = Integer.parseInt(message.getHeaders()
        .getOrDefault(CONTENT_LENGTH, "0"));

    // Config.GZIP_MAXSIZE < Config.CONTENT_LENGTH_THRESHOLD;

    if (length != 0) {
      // -------------------- 1. Content Encoding -------------------- //
      /*  Only support gzip.                                              */
      /*  Only used when the size is smaller than Config.GZIP_MAXSIZE     */
      /*  to avoid memory overload.                                       */
      if (acceptEncoding.contains(GZIP) && length <= Config.GZIP_MAXSIZE) {
        Log.debug("Content encoded with gzip");
        EncodeStrategy gzipStrategy = new ContentGzipEncodeStrategy();
        upperStrategy = gzipStrategy.connect(message.getHeaders(), upperStrategy);
      }


      if (transferEncodings != null
          && length < Config.CONTENT_LENGTH_THRESHOLD) {
        // -------------------- 2. Transfer Encoding -------------------- //
        /* Now only support chunked encoding.                               */
        /* Using chunked encoding only when the size is smaller than        */
        /* Config.CONTENT_LENGTH_THRESHOLD.                                 */
        for (String te : transferEncodings) {
          if (!strategyMap.containsKey(te))
            Log.panic("Unsupported transfer-encoding[%s]!".formatted(te));
          upperStrategy = strategyMap.get(te).connect(message.getHeaders(), upperStrategy);
        }
      }
    }

    // -------------------- 3. Send out start line and headers -------------------- //
    written += flush(
        new SourceEncodeStrategy(message.getStartLineAndHeadersAsStream())
    );

    // -------------------- 4. Send out -------------------- //
    written += flush(upperStrategy);

    if (socket != null)
      Log.logSocket(socket, "Message sent %f KB ".formatted((double) written / (1 << 10)));

    return written;
  }

  private int flush(EncodeStrategy strategy)
      throws ExecutionException, InterruptedException, IOException {
    int written = 0;

    if (socket != null) {
      for (byte[] bytes; (bytes = strategy.readBytes()).length != 0; ) {
        bb.clear();
        bb.put(bytes);
        bb.flip();
        try {
          for (int cur = 0, delta; cur < bb.limit(); cur += delta) {
            bb.position(cur);
            var future = socket.write(bb);
            delta = future.get();
          }
          written += bb.limit();
        } catch (WritePendingException e) {
          e.printStackTrace();
        }
      }
    } else {
      assert byteArrayQueue != null;
      byte[] bytes = strategy.readAllBytes();
      byteArrayQueue.offer(bytes);
      written = bytes.length;
    }

    return written;
  }

}
