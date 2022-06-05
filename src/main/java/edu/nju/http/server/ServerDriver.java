package edu.nju.http.server;

import edu.nju.http.exception.InvalidCommandException;
import edu.nju.http.util.ArgIterator;
import edu.nju.http.util.Config;

public class ServerDriver {
    public static final String HELP = """
            SYNOPSIS
                ~   [-a <ADDRESS>] [-p <PORT>] [--keep-alive]
                    [-t <TIMEOUT>]
                    
            OPTIONS
                -a <ADDRESS>    Bind the server to the specified IPv4 address.
                                The default value is 127.0.0.1
                
                -p <PORT>       Bind the server to the specified port number.
                                The default value is 8080
                                
                --keep-alive    Enable keep-alive.
                
                -t <TIMEOUT>    Socket timeout.
                                The default value is 10000
            """;

    public static void main(String[] args) {
        try {
            ArgIterator ai = new ArgIterator(args, "-");

            String hostname = "127.0.0.1";
            int port = 8080;
            boolean keepAlive = Config.DEFAULT_KEEP_ALIVE;
            int timeout = Config.DEFAULT_TIMEOUT;

            while (ai.hasNext()) {
                String opt = ai.next();
                switch (opt) {
                    case "-a" -> hostname = ai.next();
                    case "-p" -> port = Integer.parseInt(ai.next());
                    case "--keep-alive" -> keepAlive = true;
                    case "-t" -> timeout = Integer.parseInt(ai.next());
                    default -> throw new InvalidCommandException("Invalid token at \"%s\"".formatted(opt));
                }
            }

            HttpServer server = new HttpServer(hostname, port);
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
