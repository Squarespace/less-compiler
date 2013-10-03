package com.squarespace.v6.template.less;


public class LessException extends Exception {

  private ErrorInfo info;
  
  public LessException(ErrorInfo info) {
    super(info.getMessage());
    this.info = info;
  }
  
  public ErrorInfo errorInfo() {
    return info;
  }

}
