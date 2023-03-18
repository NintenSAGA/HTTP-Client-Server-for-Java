package edu.nju.http.server.target;

import edu.nju.http.message.ResponseMessageFactory;


public abstract class TargetSet {
  protected static final ResponseMessageFactory factory;

  static {
    factory = ResponseMessageFactory.getInstance();
  }
}
