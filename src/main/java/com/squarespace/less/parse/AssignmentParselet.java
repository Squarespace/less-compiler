package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.ENTITY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.Node;


/**
 * Parses a NAME '=' ENTITY sequence.
 */
public class AssignmentParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark position = stm.mark();
    if (!stm.matchWord()) {
      return null;
    }
    String name = stm.token();

    stm.skipWs();
    if (!stm.seekIf(Chars.EQUALS_SIGN)) {
      stm.restore(position);
      return null;
    }
    
    Node value = stm.parse(ENTITY);
    if (value == null) {
      stm.restore(position);
      return null;
    }
    
    return new Assignment(name, value);
  }
  
}
