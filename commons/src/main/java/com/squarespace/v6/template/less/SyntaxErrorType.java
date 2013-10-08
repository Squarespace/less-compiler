package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.core.Constants.NULL_PLACEHOLDER;

import java.util.Map;

import com.squarespace.v6.template.jsont.MapFormat;


/**
 * Errors thrown during the parse phase of the compile are syntax errors.
 */
public enum SyntaxErrorType implements ErrorType {

  EXPECTED
  ("Expected %(arg0)s"),

  GENERAL
  ("%(arg0)s"),
  
  INCOMPLETE_PARSE
  ("Unable to complete parse."),
  
  JAVASCRIPT_DISABLED
  ("This version of LESS lacks Javascript support."),
  
  MIXED_DELIMITERS
  ("Cannot mix semicolon and comma as delimiters"),
  
  QUOTED_BARE_LF
  ("Quoted string contains a bare line feed")

  ;
  
  private static final String PREFIX = "SyntaxError %(code)s ";

  private MapFormat prefixFormat;
  
  private MapFormat messageFormat;
  
  private SyntaxErrorType(String rawFormat) {
    this.prefixFormat = new MapFormat(PREFIX, NULL_PLACEHOLDER);
    this.messageFormat = new MapFormat(rawFormat, NULL_PLACEHOLDER);
  }

  public String prefix(Map<String, Object> params) {
    return prefixFormat.apply(params);
  }
  
  public String message(Map<String, Object> params) {
    return messageFormat.apply(params);
  }
  
}
