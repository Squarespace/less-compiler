package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.UnicodeRange;


public class UnicodeRangeParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (stm.peek() == 'U' && stm.matchUnicodeRange()) {
      return new UnicodeRange(stm.token());
    }
    return null;
  }
  
}
