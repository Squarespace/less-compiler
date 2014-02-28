package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.CONDITION;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Node;


public class GuardParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    stm.skipWs();
    if (!stm.matchWhen()) {
      return null;
    }
    Guard guard = new Guard();
    Node cond = null;
    while ((cond = stm.parse(CONDITION))!= null) {
      guard.add((Condition)cond);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) { 
        break;
      }
    }
    return guard;
  }

}
