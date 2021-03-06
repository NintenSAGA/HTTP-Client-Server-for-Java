package util;

import edu.nju.http.client.HttpClient;
import edu.nju.http.util.Config;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {

    public static void testAndCompareWebPage(String hostName, int port, String ... targets) throws IOException, InterruptedException {
        HttpClient client = new HttpClient(hostName, port, true);

        for (var target : targets) {
            comparePage(client, hostName, port, target);
        }
    }
    public static void testAndCompareWebPage(String hostName, String ... targets) throws IOException, InterruptedException {
        testAndCompareWebPage(hostName, 80, targets);
    }

    public static void comparePage(HttpClient client, String hostName, int port, String target) throws IOException, InterruptedException {
        String param = null;
        var hrm = client.get(target);

        String statusCode = hrm.getStartLineAndHeaders().split(" ")[1];


        String tempPath = Config.TEST_CACHE;
        Files.createDirectories(Path.of(tempPath));

        var f = File.createTempFile("Actual" + hostName, ".html", Path.of(tempPath).toFile());
        var expF = File.createTempFile("Expected" + hostName, ".html", Path.of(tempPath).toFile());

        String expFName = expF.getName();

        try (var out = new FileOutputStream(f)) {
            out.write(hrm.getBodyAsBytes());
        }

        String cmd = "/usr/local/bin/wget %s -O %s".formatted(hostName + ":" + port + target, tempPath + "/" + expFName);
//        System.out.println(cmd);

        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();

        try (var actIn = new FileInputStream(f)) {
            try (var expIn = new FileInputStream(expF)) {
                Assertions.assertArrayEquals(expIn.readAllBytes(), actIn.readAllBytes());
            }
        }

        Files.delete(f.toPath());
        Files.delete(expF.toPath());
    }
}
