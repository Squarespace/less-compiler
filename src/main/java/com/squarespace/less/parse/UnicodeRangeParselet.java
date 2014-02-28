package com.squarespace.less.parse;

import com.squarespace.less.model.Node;
import com.squarespace.less.model.UnicodeRange;


public class UnicodeRangeParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (stm.peek() == 'U' && stm.matchUnicodeRange()) {
      return new UnicodeRange(stm.token());
    }
    return null;
  }
  
}
