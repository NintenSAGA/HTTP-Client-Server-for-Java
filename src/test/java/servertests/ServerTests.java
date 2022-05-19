package servertests;

import lombok.Builder;
import org.junit.jupiter.api.Test;
import server.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.fail;

public class ServerTests {

    @Test
    public void launchTest() {
        try {
            HttpServer httpServer = new HttpServer();
            httpServer.launch();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void shutdownTest() {
        try {
            HttpServer httpServer = new HttpServer();
            CompletableFuture.runAsync(() -> httpServer.launch(true, 10000));
            sleep(2000);
            httpServer.shutdown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void sendAndReceiveTest() {
        try {
            HttpServer httpServer = new HttpServer();
            CompletableFuture.runAsync(httpServer::launch);
            sleep(1000);
            Process p = Runtime.getRuntime().exec("""
                    /usr/bin/curl --location --request GET http://127.0.0.1:8080/moved \\
                    --header "Content-Type: text/plain" \\
                    --data-raw "This is a test message""");
            p.waitFor();
            httpServer.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
