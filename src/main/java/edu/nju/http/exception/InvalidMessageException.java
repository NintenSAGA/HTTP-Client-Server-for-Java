package edu.nju.http.exception;

import edu.nju.http.util.Log;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.stream.Collectors;

public class InvalidMessageException extends Exception{
    String msg;

    public InvalidMessageException(Object ... msgs) {
        msg = Arrays.stream(msgs).map(Object::toString).collect(Collectors.joining());
    }

    public void printMsg(AsynchronousSocketChannel socket) {
        if (msg != null) {
            if (socket != null)
                Log.logSocket(socket, "Invalid Message: " + msg);
            else
                Log.debug("Invalid Message: " + msg);
        }

    }

    @Override
    public String getMessage() {
        return msg;
    }
}
