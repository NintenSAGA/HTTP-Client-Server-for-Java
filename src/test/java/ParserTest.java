import exception.InvalidMessageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.HttpResponseMessage;
import server.ResponseMessageFactory;
import util.Log;
import util.parser.MessageParser;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test cases for Request Message Parser and Response Message Parser<br/>
 * Author: 邱兴驰 李佳骏
 */
public class ParserTest {

    String longText = """
            ### 05-16 神奇的EVENT ERROR

            普通运行没问题，但是一开debug log运行就会EVENT ERROR。

            一开始怀疑是fork有问题，但反复调试后发现似乎没有影响；接下来又开始怀疑是中断嵌套的处理，但依旧没有问题；随后还怀疑了copy on write，依旧没有结果。

            观察日志，发现每次错误都在fork的中断嵌套周围，即若fork前后发生中断嵌套，随后产生的子进程一旦被调度就会ERROR

            之前一直怀疑是fork后中断会破坏栈上的数据，但在反复推理分析后发现问题出在fork前的中断。

            我的中断嵌套处理会在syscall完后恢复current的context为原来的值，而倘若在fork运行前发生中断嵌套，则fork中会拷贝错误的context内容（此时current的context指针指向内核运行栈），因此运行就会导致Global Protection Fault""";

    private void testText(HttpResponseMessage hrm) {
        MessageParser parser = new MessageParser(hrm.flatMessageToBinary());
        try {
            String msg = parser.parseToHttpResponseMessage().getBody();
            assertEquals(longText, msg);
        } catch (InvalidMessageException e) {
            e.printMsg(null);
            fail();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test()
    @DisplayName("Content-Length test")
    public void test1() {
        HttpResponseMessage hrm = ResponseMessageFactory.getInstance().produce(200);
        hrm.setBodyAsPlainText(longText);
        testText(hrm);
    }

    @Test()
    @DisplayName("Chunked test")
    public void test2() {
        HttpResponseMessage hrm = ResponseMessageFactory.getInstance().produce(200);
        hrm.setBodyAsPlainTextChunked(longText);
        testText(hrm);
    }
}
