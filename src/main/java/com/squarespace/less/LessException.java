package com.squarespace.less;

import java.util.ArrayDeque;
import java.util.Deque;

import com.squarespace.less.model.Node;


/**
 * Sole exception thrown by internals. Actual error type and internal stack are
 * part of the ErrorInfo structure.
 */
public class LessException extends Exception {

  /** Collects additional context about where the primary error occurred */
  private final Deque<Node> errorContext = new ArrayDeque<>(5);
  
  private final ErrorInfo info;
  
  public LessException(ErrorInfo info) {
    super(info.getMessage());
    this.info = info;
  }
  
  public ErrorInfo primaryError() {
    return info;
  }
  
  public Deque<Node> errorContext() {
    return errorContext;
  }

  public void push(Node node) {
    errorContext.push(node);
  }

}
