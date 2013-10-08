package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.core.SyntaxErrorMaker.general;
import static com.squarespace.v6.template.less.parse.Parselets.ENTITY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Shorthand;


public class ShorthandParselet implements Parselet {

  private static final String PARSE_ERROR = "Shorthand pattern matched but failed to complete parse";
  
  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.peekShorthand()) {
      return null;
    }
    Node left = stm.parse(ENTITY);
    stm.seekIf(Chars.SLASH);
    Node right = stm.parse(ENTITY);
    if (left == null || right == null) {
      throw stm.parseError(new LessException(general(PARSE_ERROR)));
    }
    return new Shorthand(left, right);
  }

}
