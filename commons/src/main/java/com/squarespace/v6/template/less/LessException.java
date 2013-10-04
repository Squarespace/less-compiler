package com.squarespace.v6.template.less;

import java.util.ArrayDeque;
import java.util.Deque;

import com.squarespace.v6.template.less.model.Node;


public class LessException extends Exception {

  /** Collects additional context about where the primary error occurred */
  private Deque<Node> errorContext;
  
  private ErrorInfo info;
  
  private Node node;
  
  public LessException(ErrorInfo info) {
    super(info.getMessage());
    this.info = info;
  }
  
  public ErrorInfo primaryError() {
    return info;
  }
  
  public Node primaryNode() {
    return node;
  }
  
  public Deque<Node> errorContext() {
    return errorContext;
  }

  public void push(Node node) {
    if (errorContext == null) {
      errorContext = new ArrayDeque<>();
    }
    errorContext.push(node);
  }

}
