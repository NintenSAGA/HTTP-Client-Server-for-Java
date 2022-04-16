package clienttests;

import client.HttpClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.HttpServer;
import util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestTest {
    static HttpServer server;
    static CompletableFuture<Void> future;
    static BufferedReader br;

    @BeforeAll
    static void setUp() throws IOException {
        Log.discardErr();
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
        Log.testInit(new PrintStream(pipedOutputStream));

        br = new BufferedReader(new InputStreamReader(pipedInputStream));
        server = new HttpServer();
        future = CompletableFuture.runAsync(() -> server.launch());
    }


    @Test
    @DisplayName("GET Method test")
    public void getTest() throws IOException {
        new HttpClient().get("/test", null);
        assertEquals("HTTP/1.1 200 OK", br.readLine());
    }

    @Test
    @DisplayName("POST Method test")
    public void postTest() throws IOException {
        new HttpClient().post("/register", "name=hhh&password=996", null);
        assertEquals("HTTP/1.1 201 Created", br.readLine());
    }


    @AfterAll
    static void cleanUp() throws ExecutionException, InterruptedException, IOException {
        server.shutdown();
        br.close();
    }
}
