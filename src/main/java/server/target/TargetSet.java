package server.target;

import server.ResponseMessageFactory;


public abstract class TargetSet {
    protected static final ResponseMessageFactory factory;

    static {
        factory = ResponseMessageFactory.getInstance();
    }
}
