package util;

import java.util.function.UnaryOperator;

public class Log {
    private final static String COLOR_TEMPLATE = "\033[38;5;%dm%s\033[m";
    private final static UnaryOperator<String> RED = s -> COLOR_TEMPLATE.formatted(168, s);
    private final static UnaryOperator<String> PURPLE = s -> COLOR_TEMPLATE.formatted(69, s);

    private final static UnaryOperator<String> PROMPT = RED;
    private final static UnaryOperator<String> BODY = PURPLE;

    /**
     * Method used to print debug information to stderr
     */
    public static void debug(String msg) {
        System.err.print(PROMPT.apply("DEBUG: "));
        System.err.println(BODY.apply(msg));
    }

    public static void testExpect(String prompt, String exp, String act) {
        System.err.print(PROMPT.apply(prompt + ": "));
        System.err.println(BODY.apply("Expected: %s\tActually: %s%n".formatted(exp, act)));
    }
}
