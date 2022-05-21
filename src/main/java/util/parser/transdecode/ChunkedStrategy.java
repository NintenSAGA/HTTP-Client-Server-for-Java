package util.parser.transdecode;

import exception.InvalidMessageException;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static util.consts.Headers.content_length;

public class ChunkedStrategy extends TransDecodeStrategy {
    @Override
    public byte[] getBody(Map<String, String> headers) throws InvalidMessageException {
        if (headers.containsKey(content_length))
            throw new InvalidMessageException("containing Content-Length header!");

        try {
            Queue<byte[]> queue = new LinkedList<>();
            int sumLen = 0;

            for (int chunkLen; (chunkLen = Integer.parseInt(reader.readLine(), 16)) != 0; ) {
                sumLen += chunkLen;
                byte[] bytes = reader.readNBytes(chunkLen);
                queue.offer(bytes);
                reader.readNBytes(2); //CRLF
            }
            reader.readNBytes(2); //CRLF

            ByteBuffer byteBuffer = ByteBuffer.allocate(sumLen);
            while (!queue.isEmpty())
                byteBuffer.put(queue.poll());

            headers.put(content_length, String.valueOf(sumLen));

            return byteBuffer.array();
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("Invalid chunk length[%s]".formatted(e.getMessage()));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            throw new InvalidMessageException("Message Error!");
        }


    }
}
