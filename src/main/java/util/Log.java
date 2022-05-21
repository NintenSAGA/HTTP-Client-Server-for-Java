package util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.channels.AsynchronousSocketChannel;
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
    private static PrintWriter err;
    private static PrintWriter out;
    private static PrintWriter test;


    static {
        enabled = false;
        assert enabled = true;
        err     = new PrintWriter(System.err, true);
        out     = new PrintWriter(System.out, true);
        test    = null;
    }

    /**
     * Initialize the test writer
     * @param testStream output stream for test writer
     */
    synchronized public static void testInit(PrintStream testStream) {
        test = new PrintWriter(testStream, true);
    }

    synchronized public static void setErrStream(PrintStream errStream) {
        err = new PrintWriter(errStream, true);
    }

    synchronized public static void setOutStream(PrintStream outStream) {
        out = new PrintWriter(outStream, true);
    }

    // ==================== Logging ==================== //

    /**
     * Write test message. Will do nothing if testInit hasn't performed
     * @param msg test message
     */
    synchronized public static void testInfo(Object ... msg) {
        String s = Arrays.stream(msg).map(Object::toString).collect(Collectors.joining(""));
        if (test == null)   Log.debug(s);
        else                test.println(s);
    }

    /**
     * Print debug information to stderr with custom prompt<br/>
     * Format: [prompt]: ...
     * <br/>Enabled when assertion is enabled.
     * @param prompt Prompt word
     */
    synchronized public static void debugPrompt(String prompt, Object ... msg) {
        if (!enabled) return;
        err.print(PROMPT.apply("%s: ".formatted(prompt)));
        err.println(BODY.apply(Arrays.stream(msg).map(Object::toString).collect(Collectors.joining(""))));
    }

    /**
     * Print log information to stdout with custom prompt<br/>
     * Format: [prompt]: ...
     * @param prompt Prompt word
     */
    synchronized public static void logPrompt(String prompt, Object ... msg) {
        out.print(PURPLE.apply("%s: ".formatted(prompt)));
        out.println(Arrays.stream(msg).map(Object::toString).collect(Collectors.joining("")));
    }

    /**
     * Print debug information to stderr. <br/>
     * Format: DEBUG: ...
     * <br/>Enabled when assertion is enabled.
     */
    public static void debug(Object ... msg) {
        debugPrompt("DEBUG", msg);
    }

    public static void logServer(Object ... msg) {
        logPrompt("SERVER", msg);
    }

    public static void logClient(Object ... msg) {
        logPrompt("CLIENT", msg);
    }

    public static void logSocket(AsynchronousSocketChannel socket, Object ... msg) {
        String prompt;
        try {
            prompt = socket.isOpen() ? socket.getRemoteAddress().toString().replace("/", "") : "null";
        } catch (IOException e) {
            prompt = "null";
        }
        logPrompt("SOCKET[%s]".formatted(prompt), msg);
    }

    synchronized public static void logSocketLoading(AsynchronousSocketChannel socket, Object ... msg) {
        String prompt;
        try {
            prompt = socket.isOpen() ? socket.getRemoteAddress().toString().replace("/", "") : "null";
        } catch (IOException e) {
            prompt = "null";
        }
        out.print("\r");
        logPrompt("SOCKET[%s]".formatted(prompt), msg);
    }

    public static void panic(Object ... msg) {
        debugPrompt("Panic", msg);
        assert false;
    }

    // ==================== Testing ==================== //

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
    public static void showExpectDiff(String prompt, Object exp, Object act) {
        if (!enabled) return;
        err.print(PROMPT.apply(prompt + ": "));
        err.println(BODY.apply("Expected: %s\tActual: %s%n".formatted(exp.toString(), act.toString())));
    }

    public static void discardErr() throws FileNotFoundException {
        if (!"Windows".equals(System.getProperty("os.name")))
            setErrStream(new PrintStream("/dev/null"));
        else
            setErrStream(new PrintStream("nul"));
    }

    public static void discardStdout() throws FileNotFoundException {
        if (!"Windows".equals(System.getProperty("os.name")))
            setOutStream(new PrintStream("/dev/null"));
        else
            setOutStream(new PrintStream("nul"));
    }
}
