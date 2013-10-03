package com.squarespace.v6.template.less;

import java.util.Map;


public interface ErrorType {

  public abstract String prefix(Map<String, Object> params);
  
  public abstract String message(Map<String, Object> params);

}
