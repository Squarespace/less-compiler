package com.squarespace.less.parse;


public class Mark {

  public int index;

  public int lineOffset;

  public int charOffset;

  @Override
  public String toString() {
    return "at line " + (lineOffset + 1) + " character " + (charOffset + 1);
  }

}
