import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;
import util.Config;
import util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

public class CustomTests {

    @Test
    public void sample() {
        Log.debug("Premature optimization is the root of all evil ——D. E. Knuth");
        Log.debug("虽然不太愿意承认，但始终假设自己的代码是错的。");
        Log.debug("软件是需求 (规约) 在计算机数字世界的投影。");
        Log.debug("把程序需要满足的条件用 assert 表达出来。");
        Log.debug("如果出现了莫名其妙的异常、虚拟机神秘重启等情况不要惊慌，机器永远是对的，坐下来调代码吧。");
        Log.debug("我们犯下的 bug 是 fault，它在运行时导致系统某个数值不符合预期 (specification), 这是一个" +
                " error，而只有最终虚拟机神秘卡死/重启/输出奇怪内容，我才真正证明了程序里 bug 的存在 (failure)。");
    }

    @Test
    public void customTestJson() {
        try {
            Process p = Runtime.getRuntime().exec("""
                    /usr/bin/curl --location --request GET 'http://127.0.0.1:8080/moved' \\
                    --header 'Content-Type: text/plain' \\
                    --data-raw 'This is a test message'""");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getErrorStream())
            );
            System.out.println(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void customNio2Test() throws IOException, ExecutionException, InterruptedException {
        var socket = AsynchronousServerSocketChannel.open();
        socket.bind(new InetSocketAddress("127.0.0.1", 8080));
        var future = socket.accept();
        var s = future.get();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1 << 20);
        s.read(byteBuffer);
        byteBuffer.flip();
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(byteBuffer.array()))
        );
        for (String line; !(line = bufferedReader.readLine()).isEmpty(); ) {
            System.out.println(line);
        }
    }
}
