import client.HttpRequestMessage;
import org.junit.jupiter.api.Test;
import server.TargetHandler;

public class TestHandler {

    @Test
    public void checkTarget() {
        TargetHandler targetHandler = TargetHandler.get();
        targetHandler.handle(new HttpRequestMessage("GET", "/test"));
        targetHandler.handle(new HttpRequestMessage("GET", "/dhqwwidq"));
    }
}
