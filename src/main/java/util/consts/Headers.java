package util.consts;

import java.util.Locale;

public class Headers {

    // ==================== Encodings ==================== //
    
    // -------------------- Transfer-Encoding -------------------- //
    public final static String TRANSFER_ENCODING = "Transfer-Encoding";
    public final static String transfer_encoding = TRANSFER_ENCODING.toLowerCase(Locale.ROOT);

    // -------------------- Content-Encoding -------------------- //
    public final static String CONTENT_ENCODING = "Content-Encoding";
    public final static String content_encoding = CONTENT_ENCODING.toLowerCase(Locale.ROOT);

    // -------------------- Accept-Encoding -------------------- //
    public final static String ACCEPT_ENCODING  = "Accept-Encoding";
    public final static String accept_encoding  = ACCEPT_ENCODING.toLowerCase(Locale.ROOT);

    // -------------------- formats -------------------- //
    public final static String CONTENT_LENGTH   = "Content-Length";
    public final static String content_length   = CONTENT_LENGTH.toLowerCase(Locale.ROOT);

    public final static String CHUNKED          = "chunked";
    public final static String DEFLATE          = "deflate";
    public final static String GZIP             = "gzip";

    // ==================== Location ==================== //
    public static final String LOCATION         = "Location";
    
    
}
