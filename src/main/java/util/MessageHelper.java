package util;

import java.io.BufferedReader;
import java.io.IOException;

public class MessageHelper {

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
}
