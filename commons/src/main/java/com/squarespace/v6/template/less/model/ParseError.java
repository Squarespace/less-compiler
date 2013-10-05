package com.squarespace.v6.template.less.model;

import java.nio.file.Path;


/**
 * A vehicle to pass parser errors up through the execution chain.
 * Used when a template being executed imports another template which
 * fails to parse.  We need to show the execution context, followed by
 * the full parse error.  This node lets us pass the full parse error
 * message.
 */
public class ParseError extends BaseNode {

  private Path filePath;
  
  private String errorMessage;

  public Path filePath() {
    return filePath;
  }
  
  public String errorMessage() {
    return errorMessage;
  }

  public void filePath(Path path) {
    this.filePath = path;
  }
  
  public void errorMessage(String message) {
    this.errorMessage = message;
  }
  
  @Override
  public NodeType type() {
    return NodeType.PARSE_ERROR;
  }

}
