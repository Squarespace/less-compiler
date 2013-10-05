package com.squarespace.v6.template.less.core;

import com.squarespace.v6.template.less.ErrorInfo;
import com.squarespace.v6.template.less.ErrorType;
import com.squarespace.v6.template.less.ExecuteErrorType;
import com.squarespace.v6.template.less.SyntaxErrorType;


/**
 * Builds error messages for parse and execute phases.
 */
public class ErrorMaker {

  private ErrorMaker() {
  }
  
  public static ErrorInfo argCount(String name, int expected, int actual) {
    return error(ExecuteErrorType.ARG_COUNT).name(name).arg0(expected).arg1(actual);
  }
  
  public static ErrorInfo argNamedNotFound(Object name) {
    return error(ExecuteErrorType.ARG_NAMED_NOTFOUND).name(name);
  }

  public static ErrorInfo importError(Object path, Object msg) {
    return error(ExecuteErrorType.IMPORT_ERROR).arg0(path).arg1(msg);
  }
  
  public static ErrorInfo incompleteParse() {
    return error(SyntaxErrorType.INCOMPLETE_PARSE);
  }
  
  private static ErrorInfo error(ErrorType type) {
    ErrorInfo info = new ErrorInfo(type);
    info.code(type);
    return info;
  }
}
