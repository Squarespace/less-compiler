package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.SELECTOR;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;


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
      stm.skipWs();
      selector = stm.parse(SELECTOR);
    }

    return group;
  }

}
