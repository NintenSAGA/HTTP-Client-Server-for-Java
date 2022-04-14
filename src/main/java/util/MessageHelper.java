package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessageHelper {
    private static final SimpleDateFormat sdf;
    static {
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Read body with fixed Content-Length (in bytes) <br/>
     * This method is used because Reader classes in Java read input stream
     * to 16-bit char (Unicode) instead of 8-bit char, while the Content-Length
     * header of http represents length in bytes.
     * @param br BufferedReader used by the upper flow
     * @param lenInBytes The value of Content-Length header
     * @return Body String
     */
    public static String readBody(BufferedReader br, int lenInBytes) throws IOException {
        assert (br != null && lenInBytes > 0);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lenInBytes; i++) {
            int code = br.read();
            sb.append((char) code);
            code /= 128;
            while (code > 0) {
                i++;
                code /= 128;
            }
        }
        return sb.toString();
    }

    /**
     * Get the current time in GMT format
     */
    public static String getTime() {
        return sdf.format(Calendar.getInstance().getTime());
    }

    /**
     * Get the current time in GMT format
     */
    public static String getTime(Calendar cal) {
        return sdf.format(cal.getTime());
    }
}
