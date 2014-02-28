package com.squarespace.less.parse;

import com.squarespace.less.core.CharClass;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ratio;


public class RatioParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (CharClass.digit(stm.peek()) && stm.matchRatio()) {
      return new Ratio(stm.token());
    }
    return null;
  }
  
}
