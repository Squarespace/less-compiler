package com.squarespace.less.model;

import com.squarespace.less.core.Buffer;

/**
 * Special "color" representing transparency. Acts as #000000 when part
 * of a color math operation, otherwise always emitted as a keyword.
 */
public class TransparentColor extends RGBColor {

  private static final String KEYWORD = "transparent";

  public TransparentColor() {
    super(0, 0, 0);
  }

  public String keyword() {
    return KEYWORD;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(KEYWORD);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" TRANSPARENT_COLOR [fake rgb] ").append(' ');
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TransparentColor) {
      return true;
    }
    if (obj instanceof BaseColor) {
      // Compare as color in other circumstances
      return obj.equals(this);
    }
    return false;
  }
}
