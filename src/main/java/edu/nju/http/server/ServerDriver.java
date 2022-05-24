package edu.nju.http.server;

import edu.nju.http.exception.InvalidCommandException;
import edu.nju.http.util.ArgIterator;
import edu.nju.http.util.Config;

public class ServerDriver {
    public static final String HELP = """
            SYNOPSIS
                ~   [-p <PORT>] [--keep-alive]
                    [-t <TIMEOUT>]
                    
            OPTIONS
                -p <PORT>       Set up the server with the specified port number.
                                The default value is 8080
                                
                --keep-alive    Enable keep-alive.
                
                -t <TIMEOUT>    Socket timeout.
                                The default value is 10000
            """;

    public static void main(String[] args) {
        try {
            ArgIterator ai = new ArgIterator(args, "-");

            int port = 8080;
            boolean keepAlive = Config.DEFAULT_KEEP_ALIVE;
            int timeout = Config.DEFAULT_TIMEOUT;

            while (ai.hasNext()) {
                String opt = ai.next();
                switch (opt) {
                    case "-p" -> port = Integer.parseInt(ai.next());
                    case "--keep-alive" -> keepAlive = true;
                    case "-t" -> timeout = Integer.parseInt(ai.next());
                    default -> throw new InvalidCommandException("Invalid token at \"%s\"".formatted(opt));
                }
            }

            HttpServer server = new HttpServer("127.0.0.1", port);
            server.launch(keepAlive, timeout);

        } catch (InvalidCommandException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.err.println(HELP);
        }
    }
}
