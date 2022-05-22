package clienttests;

import client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.HttpServer;
import util.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestTest {
    static HttpServer server;
    static CompletableFuture<Void> future;
    static BufferedReader br;

    @BeforeEach
    void setUp() throws IOException {
        server = new HttpServer();
        future = CompletableFuture.runAsync(() -> server.launch(true, 10000));
    }


    @Test
    @DisplayName("GET Method test")
    public void getTest() throws IOException {
        var hrm = new HttpClient().get("/test", null);
        String startLine = hrm.flatMessage().split("\r\n")[0];
        assertEquals("HTTP/1.1 200 OK", startLine);
    }

    @Test
    @DisplayName("POST Method test")
    public void postTest() throws IOException {
        var hrm = new HttpClient().post("/register", "name=hhh&password=996", null);
        String startLine = hrm.flatMessage().split("\r\n")[0];
        assertEquals("HTTP/1.1 201 Created", startLine);
    }

    @Test
    @DisplayName("Redirect Test")
    public void redirectTest() throws IOException {
        var hrm = new HttpClient("127.0.0.1", 8080, true)
                .get("/moved", null);
        String body = hrm.getBodyAsString();
        assertEquals("You got the place!!!", body);
    }

    @Test
    @DisplayName("304 Cache Test")
    public void cacheTest() throws IOException, InterruptedException {
        HttpClient client = new HttpClient();
        client.get("/OS/2022/", null);

        client = new HttpClient();
        var hrm = client.get("/OS/2022/", null);

        assert "304".equals(hrm.getStartLineAndHeaders().split(" ")[1]);

        Util.testAndCompareWebPage("127.0.0.1", 8080, "/OS/2022/");
    }


    @AfterEach
    void cleanUp() {
        server.shutdown();
    }
}
