package util.packer.transencode;

import util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class TransEncodeStrategy {
    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected Map<String, String> headers;

    public InputStream encode(Map<String, String> headers, InputStream stream) throws IOException {
        PipedInputStream pis = new PipedInputStream();

        this.inputStream = stream;
        this.outputStream = new PipedOutputStream(pis);
        this.headers = headers;

        headerEditing();
        CompletableFuture.runAsync(this::encodeProxy);

        return pis;
    }

    protected abstract void headerEditing() throws IOException;

    protected abstract void encode() throws IOException;

    private void encodeProxy() {
        try {
            encode();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                outputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.panic("Writing to PipedOutputStream has failed too much times!");
            }
        }
    }
}
