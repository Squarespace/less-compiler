package com.squarespace.less.parse;

import static com.squarespace.less.core.SyntaxErrorMaker.general;
import static com.squarespace.less.parse.Parselets.ENTITY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Shorthand;


public class ShorthandParselet implements Parselet {

  private static final String PARSE_ERROR = "Shorthand pattern matched but failed to complete parse";

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.peekShorthand()) {
      return null;
    }
    Mark position = stm.mark();
    Node left = stm.parse(ENTITY);
    if (!stm.seekIf(Chars.SLASH)) {
      stm.restore(position);
      return null;
    }
    Node right = stm.parse(ENTITY);
    if (left == null || right == null) {
      throw stm.parseError(new LessException(general(PARSE_ERROR)));
    }
    return new Shorthand(left, right);
  }

}
