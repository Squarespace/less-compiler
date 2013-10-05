package com.squarespace.v6.template.less;

import com.squarespace.v6.template.less.core.LessUtils;


public enum CompilerOption {

  IMPORT_MARKERS,
  MIXIN_MARKERS
  ;

  public static CompilerOption fromString(String str) {
    return valueOf(str.toUpperCase());
  }
  
  public static String options() {
    return LessUtils.enumValueList(CompilerOption.class, true);
  }

}
