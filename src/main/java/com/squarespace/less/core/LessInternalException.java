package com.squarespace.less.core;


/**
 * Represents a serious invalid code exception, indicating a likely bug within
 * the framework itself.
 */
public class LessInternalException extends RuntimeException {

  static final long serialVersionUID = 1L;

  public LessInternalException(String message) {
    super(message);
  }

  public LessInternalException(String message, Throwable cause) {
    super(message, cause);
  }

}
