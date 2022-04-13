package servertests;

import org.junit.jupiter.api.Test;
import server.HttpServer;
import util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.fail;

public class MainLoopTest {

    @Test
    public void mainLoopTest() {
        try {
            HttpServer httpServer = new HttpServer();
            httpServer.launch(true, 10000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
