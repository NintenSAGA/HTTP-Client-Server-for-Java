package util.parser.transdecode;

import exception.InvalidMessageException;
import util.Log;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ChunkedStrategy extends TransDecodeStrategy {
    @Override
    public byte[] getBody(Map<String, String> headers) throws InvalidMessageException {
        try {
            Queue<byte[]> queue = new LinkedList<>();
            int sumLen = 0;

            for (int chunkLen; (chunkLen = Integer.parseInt(reader.readLine(), 16)) != 0; ) {
                sumLen += chunkLen;
//                Log.debug("%d read".formatted(chunkLen));
                byte[] bytes = reader.readNBytes(chunkLen);
//                Log.debug("%s read".formatted(new String(bytes)));
                queue.offer(bytes);
                reader.readNBytes(2); //CRLF
            }
            reader.readNBytes(2); //CRLF

            ByteBuffer byteBuffer = ByteBuffer.allocate(sumLen);
            while (!queue.isEmpty())
                byteBuffer.put(queue.poll());

            return byteBuffer.array();
        } catch (NumberFormatException e) {
            throw new InvalidMessageException("Invalid chunk length[%s]".formatted(e.getMessage()));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            throw new InvalidMessageException("Message Error!");
        }


    }
}
