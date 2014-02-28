package com.squarespace.less.exec;


/**
 * Thrown when a bad test is detected.  The problem is with the structure of the
 * test, not the LESS code itself.
 */
public class InvalidTestException extends RuntimeException {

  public InvalidTestException(String message) {
    super(message);
  }
  
}
