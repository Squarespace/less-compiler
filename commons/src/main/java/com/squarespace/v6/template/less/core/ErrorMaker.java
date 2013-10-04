package com.squarespace.v6.template.less.core;

import com.squarespace.v6.template.less.ErrorInfo;
import com.squarespace.v6.template.less.ExecuteErrorType;


/**
 * Builds error messages for parse and execute phases.
 */
public class ErrorMaker {


  public ErrorMaker() {
  }
  
  public static ErrorInfo argCount(String name, int expected, int actual) {
    return new ErrorInfo(ExecuteErrorType.ARG_COUNT).name(name).arg0(expected).arg1(actual);
  }
  
  public static ErrorInfo argNamedNotFound(Object name) {
    return new ErrorInfo(ExecuteErrorType.ARG_NAMED_NOTFOUND).name(name);
  }

  public static ErrorInfo importError(Object path, Object msg) {
    return new ErrorInfo(ExecuteErrorType.IMPORT_ERROR).arg0(path).arg1(msg);
  }
  
  
}
