package servertests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import server.HttpServer;
import util.Log;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static util.Util.*;

public class LongConnectionTest {
    static HttpServer server;
    static CompletableFuture<Void> future;

    @BeforeAll
    static void startUp() throws IOException {
        Log.discardErr();
        Log.discardStdout();
        server = new HttpServer();
        future = CompletableFuture.runAsync(() -> server.launch(true, 10000));
    }

    @Test
    void test1() throws IOException, InterruptedException {
        testAndCompareWebPage("127.0.0.1", 8080, "/lab-m3", "/jyy-os");
    }


    @AfterAll
    static void cleanUp() throws ExecutionException, InterruptedException {
        server.shutdown();
    }
}
