package util;

import java.util.ArrayList;

public class ArgIterator {
    private final
    String[] args;
    private
    int idx;

    public ArgIterator(String[] args, String prefix) {
        this.args = args;
        this.idx = 0;
        for (String first; (first = peek()) != null && !first.startsWith(prefix); )
            next();

    }

    public boolean hasNext() {
        return idx < args.length;
    }

    public String peek() {
        if (hasNext())
            return args[idx];
        else
            return null;
    }

    public String next() {
        if (hasNext())
            return args[idx++];
        else
            return null;
    }

    public String[] nextValues() {
        ArrayList<String> as = new ArrayList<>();
        for (String token; (token = peek()) != null && !token.startsWith("-"); next())
            as.add(token);
        return as.toArray(new String[0]);
    }
}
