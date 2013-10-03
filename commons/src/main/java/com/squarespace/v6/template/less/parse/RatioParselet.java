package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Ratio;


public class RatioParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (CharClass.digit(stm.peek()) && stm.matchRatio()) {
      return new Ratio(stm.token());
    }
    return null;
  }
  
}
