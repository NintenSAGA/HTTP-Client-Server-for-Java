package servertests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.nju.http.server.HttpServer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static util.Util.*;

public class KeepAliveTest {
    static HttpServer server;
    static CompletableFuture<Void> future;

    @BeforeAll
    static void startUp() throws IOException {
        server = new HttpServer();
        future = CompletableFuture.runAsync(() -> server.launch(true, 100000));
    }

    @Test
    void test1() throws IOException, InterruptedException {
        testAndCompareWebPage("127.0.0.1", 8080, "/OS/2022/", "/OS/2022/labs/M3/");
    }


    @AfterAll
    static void cleanUp() throws ExecutionException, InterruptedException {
        server.shutdown();
    }
}
