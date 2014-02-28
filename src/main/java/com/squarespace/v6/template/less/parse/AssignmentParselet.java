package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.ENTITY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Assignment;
import com.squarespace.v6.template.less.model.Node;


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
