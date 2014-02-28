package com.squarespace.less;

import com.squarespace.less.core.LessUtils;


/**
 * Enumerate flags telling the compiler to run using a particular debug mode.
 */
public enum DebugMode {

  CANONICAL
  ("Parses and emits the canonical representation of the parsed template"),

  EXPAND
  ("Expands all IMPORT and MIXIN_CALL statements, and resolves all variable references, and emits "
      + "the canonical form of the expanded template"),
  
  PARSE
  ("Parses the file and displays the internal parse tree (potentially extremely verbose)")

  ;
  
  private String description;

  private DebugMode(String desc) {
    this.description = desc;
  }
  
  public static DebugMode fromString(String str) {
    return valueOf(str.toUpperCase());
  }
  
  public static String modes() {
    return LessUtils.enumValueList(DebugMode.class, true);
  }
  
  public String description() {
    return description;
  }

}

