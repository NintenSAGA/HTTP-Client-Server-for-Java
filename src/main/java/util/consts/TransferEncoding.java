package util.consts;

import java.util.Locale;

public class TransferEncoding {
    public final static String TRANSFER_ENCODING = "Transfer-Encoding";
    public final static String transfer_encoding = TRANSFER_ENCODING.toLowerCase(Locale.ROOT);

    public final static String CONTENT_LENGTH  = "Content-Length";
    public final static String content_length  = CONTENT_LENGTH.toLowerCase(Locale.ROOT);

    public final static String CHUNKED         = "chunked";
    public final static String GZIP            = "gzip";
    public final static String DEFLATE         = "deflate";
}
