package loginsystemtests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import edu.nju.http.server.HttpServer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSystemTests {
    final static String NAME = "桐生一馬";
    final static String PASS = "DameDane";

    static HttpServer server;
    static CompletableFuture<Void> future;
    static String cookie = null;

    @BeforeAll
    static void startUp() throws IOException, InterruptedException {
        server = new HttpServer();
        future = CompletableFuture.runAsync(() -> server.launch(true, 10000));

    }

    @Test
//    @Disabled
    @DisplayName("Register Test")
    public void register() throws IOException, InterruptedException {
        var pb = new ProcessBuilder();
    pb.command("curl", "-v", "--request", "POST",  "http://127.0.0.1:8080/register",
                    "--header", "Content-Type: application/x-www-form-urlencoded",
                    "--data-urlencode", "name=%s&password=%s".formatted(NAME, PASS));
        var p = pb.start();
        p.waitFor();

        assertEquals("Registered successfully", p.inputReader().readLine());
        String line;
        while ((line = p.errorReader().readLine()) != null) {
            if (line.startsWith("< Set-Cookie:")) {
                cookie = line.replaceFirst("< Set-Cookie: ", "");
                break;
            }
        }
        assertNotNull(cookie);

        pb.command("curl", "-v", "--request", "GET",  "http://127.0.0.1:8080/status",
                    "--header", "Cookie: " + cookie);
        p = pb.start();
        p.waitFor();

        assertEquals("You've logged in as %s".formatted(NAME), p.inputReader().readLine());
    }

    @AfterAll
    static void cleanUp() throws ExecutionException, InterruptedException {
        server.shutdown();
    }
}
