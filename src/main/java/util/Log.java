package util;

import java.net.Socket;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Log {
    private final static String COLOR_TEMPLATE = "\033[38;5;%dm%s\033[m";
    private final static UnaryOperator<String> RED = s -> COLOR_TEMPLATE.formatted(168, s);
    private final static UnaryOperator<String> PURPLE = s -> COLOR_TEMPLATE.formatted(69, s);

    private final static UnaryOperator<String> PROMPT = RED;
    private final static UnaryOperator<String> BODY = PURPLE;

    private static boolean enabled;

    static {
        enabled = false;
        assert enabled = true;
    }



    /**
     * Print debug information to stderr with custom prompt<br/>
     * Format: [prompt]: ...
     * <br/>Enabled when assertion is enabled.
     * @param prompt Prompt word
     */
    synchronized public static void debugPrompt(String prompt, Object ... msg) {
        if (!enabled) return;
        System.err.print(PROMPT.apply("%s: ".formatted(prompt)));
        System.err.println(BODY.apply(Arrays.stream(msg).map(Object::toString).collect(Collectors.joining(""))));
    }

    /**
     * Print debug information to stderr. <br/>
     * Format: DEBUG: ...
     * <br/>Enabled when assertion is enabled.
     */
    public static void debug(Object ... msg) {
        debugPrompt("DEBUG", msg);
    }

    public static void debugServer(Object ... msg) {
        debugPrompt("SERVER", msg);
    }

    public static void debugClient(Object ... msg) {
        debugPrompt("CLIENT", msg);
    }

    public static void debugSocket(Socket socket, Object ... msg) {
        debugPrompt("SOCKET[%s]".formatted(socket.getRemoteSocketAddress().toString().replace("/", "")), msg);
    }

    /**
     * Compare the given expected value and actual value.<br/>
     * Return false and show difference if they're not the same.
     * <br/>Enabled when assertion is enabled.
     * @param prompt prompt word
     */
    public static boolean testExpect(String prompt, Object exp, Object act) {
        if (!enabled) return false;
        if (exp.equals(act)) return true;
        showExpectDiff(prompt, exp.toString(), act.toString());
        return false;
    }

    /**
     * Method used to show the difference between expected value and actual value
     * <br/>Enabled when assertion is enabled.
     * @param prompt prompt word
     */
    public static void showExpectDiff(String prompt, String exp, String act) {
        if (!enabled) return;
        System.err.print(PROMPT.apply(prompt + ": "));
        System.err.println(BODY.apply("Expected: %s\tActual: %s%n".formatted(exp, act)));
    }
}
