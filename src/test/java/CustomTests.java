import org.junit.jupiter.api.Test;
import util.Log;

public class CustomTests {

    @Test
    public void sample() {
        Log.debug("Premature optimization is the root of all evil ——D. E. Knuth");
        Log.debug("虽然不太愿意承认，但始终假设自己的代码是错的。");
        Log.debug("软件是需求 (规约) 在计算机数字世界的投影。");
        Log.debug("把程序需要满足的条件用 assert 表达出来。");
        Log.debug("如果出现了莫名其妙的异常、虚拟机神秘重启等情况不要惊慌，机器永远是对的，坐下来调代码吧。");
        Log.debug("我们犯下的 bug 是 fault，它在运行时导致系统某个数值不符合预期 (specification), 这是一个" +
                " error，而只有最终虚拟机神秘卡死/重启/输出奇怪内容，我才真正证明了程序里 bug 的存在 (failure)。");
    }
}
