package util;

import client.HttpClient;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {

    public static void testAndCompareWebPage(String hostName, int port, String ... targets) throws IOException, InterruptedException {
        HttpClient client = new HttpClient(hostName, port);

        for (var target : targets) {
            comparePage(client, hostName, port, target);
        }
    }
    public static void testAndCompareWebPage(String hostName, String ... targets) throws IOException, InterruptedException {
        testAndCompareWebPage(hostName, 80, targets);
    }

    public static void comparePage(HttpClient client, String hostName, int port, String target) throws IOException, InterruptedException {
        String param = null;
        var hrm = client.get(target, param);

        String statusCode = hrm.getStartLineAndHeaders().split(" ")[1];

        Assertions.assertEquals("200", statusCode);

        String dir = System.getProperty("user.dir");
        String tempPath = dir + "/temp";

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
