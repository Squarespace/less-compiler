package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.COMMENT;
import static com.squarespace.v6.template.less.parse.Parselets.SELECTOR;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Selectors;


public class SelectorsParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node selector = stm.parse(SELECTOR);
    if (selector == null) {
      return null;
    }
    
    Selectors group = new Selectors();
    while (selector != null) {
      group.add((Selector)selector);
      stm.parse(COMMENT);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
      stm.parse(COMMENT);
      selector = stm.parse(SELECTOR);
    }
    
    return group;
  }
  
}
