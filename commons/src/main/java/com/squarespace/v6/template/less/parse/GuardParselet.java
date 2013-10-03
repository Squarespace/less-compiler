package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.CONDITION;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Condition;
import com.squarespace.v6.template.less.model.Guard;
import com.squarespace.v6.template.less.model.Node;


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
