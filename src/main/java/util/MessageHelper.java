package util;

import client.HttpRequestMessage;
import server.HttpResponseMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
     * message parser method.
     * @param br
     * @param isReqOrRes isReqOrRes: true:request false:response
     * @return httpMessage
     */
    public static HttpMessage messageParser(BufferedReader br,boolean isReqOrRes) throws IOException{
        String messageLine = br.readLine();
        if (messageLine == null) throw new SocketTimeoutException();
        //处理报文第一行 开始行含有三个部分
        String[] startLine = messageLine.split(" ");
        assert startLine.length == 3;

        //用哈希表，处理首部行 字段名：值
        Map<String,String> headers = new HashMap<>();
        while (!(messageLine = br.readLine()).isEmpty()){
            //每一个首部行
            String[] header = messageLine.split(":");
            assert header.length == 2;
            String key = header[0],val = header[1];
            headers.put(key,val);
        }
        //处理实体主体
        String body = "";
        if (headers.containsKey("Content-Length"))
            body = MessageHelper.readBody(br, Integer.parseInt(headers.get("Content-Length")));


        if (isReqOrRes) {
            return new HttpRequestMessage(startLine[0],startLine[1],startLine[2],headers,body);
        }else {
            return new HttpResponseMessage(Integer.parseInt(startLine[1]),startLine[2]);
        }
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
