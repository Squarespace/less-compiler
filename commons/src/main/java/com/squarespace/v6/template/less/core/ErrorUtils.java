package com.squarespace.v6.template.less.core;

import com.squarespace.v6.template.less.ErrorInfo;
import com.squarespace.v6.template.less.ErrorType;


public class ErrorUtils {

  public static ErrorInfo error(ErrorType code) {
    ErrorInfo info = new ErrorInfo(code);
    info.code(code);
    return info;
  }

}
