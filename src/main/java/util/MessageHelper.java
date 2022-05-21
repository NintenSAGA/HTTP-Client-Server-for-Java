package util;

import client.HttpRequestMessage;
import server.HttpResponseMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class MessageHelper {
    private static final SimpleDateFormat sdf;
    static {
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
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

    public static Map<String, String> parseArgs(HttpRequestMessage msg) {
        String strArgs;
        if ("application/x-www-form-urlencoded".equals(msg.getHeaderVal("Content-Type".toLowerCase(Locale.ROOT)))) {
            strArgs = URLDecoder.decode(msg.getBodyAsString(), StandardCharsets.UTF_8);
        } else {
            String target = msg.getTarget();
            int st = 0;
            while ( st < target.length() && target.charAt(st) != '?' ) st++;
            if (st == target.length()) return null;
            strArgs = target.substring(st + 1);
        }
        return parseUrlencodedArgs(strArgs);
    }

    private static Map<String, String> parseUrlencodedArgs(String argStr) {
        try {
            return Arrays.stream(argStr.split("\\&"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(a -> a[0], a->a[1]));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
