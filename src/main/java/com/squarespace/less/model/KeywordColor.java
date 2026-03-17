package com.squarespace.less.model;

import com.squarespace.less.core.Buffer;

public class KeywordColor extends RGBColor {

  private final String keyword;

  public KeywordColor(String keyword, int r, int g, int b) {
    super(r, g, b, 1.0);
    this.keyword = keyword;
  }

  public String keyword() {
    return keyword;
  }

  /**
   * See {@link Node#deepCopy()}. Preserves the keyword subclass.
   */
  @Override
  public KeywordColor deepCopy() {
    KeywordColor result = new KeywordColor(keyword, red, green, blue);
    result.forceHex = forceHex;
    return result;
  }

  @Override
  public void modelRepr(Buffer buf) {
    super.modelRepr(buf);
    buf.append(' ').append('\'').append(keyword).append('\'');
  }
}
