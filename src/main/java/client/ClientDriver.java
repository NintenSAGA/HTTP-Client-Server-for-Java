package client;

import message.consts.WebMethods;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ClientDriver {
    public static final String HELP = """
            SYNOPSIS
                ~   <URL>
                    [-m <METHOD>] [--keep-alive] [-b <text>]
                    [-h <headers>...]
                    
            URL
                Using the generic URI syntax of:
                http://<HOSTNAME>[:PORT][/PATH][?QUERY]
                e.g.: http://jyywiki.cn/OS/2020/, http://127.0.0.1:8080/help/, http://www.google.com/search?q=uri
                The default value of the port number is 80.
                Only support HTTP protocol (not HTTPS).
                
            OPTIONS                                
                -m <METHOD>     Send with the specified web method.
                                Only supports GET and POST.
                                The default value is GET.
                                
                --keep-alive    Enable keep-alive.
                
                -b <text>       Plain text body.
                
                -h <header>...  Send with the specified headers.
                                Syntax: <key>:<value>
                                e.g.: User-Agent:AbaAba/0.1
            """;

    private static class InvalidCommandException extends Exception {
        public InvalidCommandException(String message) {
            super(message);
        }
    }

    private static class ArgIterator {
        private final
        String[] args;
        private
        int idx;

        public ArgIterator(String[] args) {
            this.args = args;
            this.idx = 0;
            for (String first; (first = peek()) != null && !first.startsWith("http://"); )
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

    public static void main(String[] args) {
        try {
            ArgIterator ai = new ArgIterator(args);

            String raw = ai.next();
            if (raw == null || raw.startsWith("-"))
                throw new InvalidCommandException("Lack of hostName");

            URI u = new URI(raw);

            String hostName     = u.getHost();
            String query        = u.getQuery();
            int port            = u.getPort();
            String path         = u.getPath();

            if (port == -1) port = 80;
            if (path == null || path.length() == 0) path = "/";

            String method       = WebMethods.GET;
            boolean keepAlive   = false;
            String body         = null;
            String[] headers    = new String[0];

            while (ai.hasNext()) {
                String opt = ai.next();
                if (opt == null) throw new InvalidCommandException("Invalid option");
                switch (opt) {
                    case "-m" -> {
                        method = ai.next();
                        assert method != null;
                        method = method.toUpperCase(Locale.ROOT);
                        assert WebMethods.GET.equals(method)
                                || WebMethods.POST.equals(method);
                    }

                    case "--keep-alive" -> {
                        keepAlive = true;
                    }

                    case "-b" -> {
                        body = ai.next();
                    }

                    case "-h" -> {
                        headers = ai.nextValues();
                    }

                    default -> {
                        throw new InvalidCommandException("Invalid token at \"%s\"".formatted(opt));
                    }
                }
            }

            HttpClient client = new HttpClient(hostName, port, keepAlive);
            var hrm = client.request(method, u.toString(), body, headers);

            System.out.println(
                    client.present(hrm)
            );

        } catch (InvalidCommandException e) {
            System.err.println(Arrays.toString(args));
            System.err.println("Error: " + e.getMessage() + "\n");
            System.err.println(HELP);
        } catch (Exception e) {
            System.err.println(Arrays.toString(args));
            e.printStackTrace();
            System.err.println(HELP);
        }


    }
}
