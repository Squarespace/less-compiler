package com.squarespace.v6.template.less;

import java.util.Map;

import com.squarespace.v6.template.jsont.MapBuilder;


/**
 * Captures type and message formatting arguments for error messages.
 */
public class ErrorInfo {

  private static final String ARG0 = "arg0";

  private static final String ARG1 = "arg1";
  
  private static final String ARG2 = "arg2";
  
  private static final String CODE = "code";
  
  private static final String NAME = "name";
  
  private static final String TYPE = "type";
  
  private MapBuilder<String, Object> builder = new MapBuilder<>();
  
  private ErrorType type;
  
  public ErrorInfo(ErrorType type) {
    this.type = type;
  }

  public ErrorInfo code(Object code) {
    return put(CODE, code);
  }
  
  public ErrorInfo arg0(Object arg) {
    return put(ARG0, arg);
  }
 
  public ErrorInfo arg1(Object arg) {
    return put(ARG1, arg);
  }
  
  public ErrorInfo arg2(Object arg) {
    return put(ARG2, arg);
  }

  public ErrorInfo name(Object name) {
    return put(NAME, name);
  }
  
  public ErrorInfo type(Object type) {
    return put(TYPE, type);
  }

  public ErrorType type() {
    return type;
  }
  
  public String getMessage() {
    Map<String, Object> params = builder.get();
    StringBuilder buf = new StringBuilder();
    buf.append(type.prefix(params)).append(type.message(params));
    return buf.toString();
  }

  private ErrorInfo put(String key, Object val) {
    builder.put(key, val);
    return this;
  }
}
