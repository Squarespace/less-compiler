package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.ELEMENT;
import static com.squarespace.less.parse.Parselets.ENTITY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.ValueElement;


public class SelectorParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Selector selector = null;

    // Parse deprecated syntax (~"@{foo}")
    stm.skipWs();
    if (stm.peek() == Chars.LEFT_PARENTHESIS) {
      stm.seek1();
      Node value = stm.parse(ENTITY);
      stm.skipWs();
      if (stm.peek() == Chars.RIGHT_PARENTHESIS) {
        stm.seek1();
        selector = new Selector();
        selector.add(new ValueElement(Combinator.DESC, value));
        return selector;
      }
      return null;
    }
    
    Node elem = null;
    while ((elem = stm.parse(ELEMENT)) != null) {
      if (selector == null) {
        selector = new Selector();
      }
      selector.add((Element)elem);
      stm.parse(COMMENT);
      if (CharClass.selectorEnd(stm.peek())) {
        break;
      }
    }
    return selector;
  }
  
}
