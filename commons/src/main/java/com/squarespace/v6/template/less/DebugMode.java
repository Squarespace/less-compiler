package com.squarespace.v6.template.less;

import com.squarespace.v6.template.less.core.LessUtils;


public enum DebugMode {
  
  CANONICAL,
  EXPAND,
  PARSE
  ;
  
  public static DebugMode fromString(String str) {
    return valueOf(str.toUpperCase());
  }
  
  public static String modes() {
    return LessUtils.enumValueList(DebugMode.class, true);
  }

}

