package edu.nju.http.message.packer.encode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static edu.nju.http.message.consts.Headers.*;

public class TransChunkedEncodeStrategy extends EncodeStrategy {
  private static final int CHUNK_SIZE = 800;     // chunk size in bytes
  private boolean done;

  @Override
  public byte[] readAllBytes() throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    while (!done) {
      outputStream.write(readBytes());
    }
    return outputStream.toByteArray();
  }

  @Override
  public byte[] readBytes() throws IOException {
    if (done) return new byte[0];

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] bytes;

    if ((bytes = upper.readNBytes(CHUNK_SIZE)).length != 0) {
      outputStream.write("%s\r\n"
          .formatted(
              Integer.toString(bytes.length, 16)
          ).getBytes());
      outputStream.write(bytes);
      outputStream.write("\r\n".getBytes());
    } else if (!done) {
      outputStream.write("0\r\n\r\n".getBytes());
      done = true;
    }

    return outputStream.toByteArray();
  }

  @Override
  @Deprecated
  protected byte[] readNBytes(int n) throws IOException {
    return readBytes();
  }

  @Override
  protected void headerEditing() throws IOException {
    headers.remove(CONTENT_LENGTH);
    headers.merge(TRANSFER_ENCODING, CHUNKED, "%s, %s"::formatted);
    done = false;
  }
}
