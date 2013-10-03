package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.SyntaxErrorType.GENERAL;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.parse.Parselets.ENTITY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Shorthand;


public class ShorthandParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.peekShorthand()) {
      return null;
    }
    Node left = stm.parse(ENTITY);
    stm.seekIf(Chars.SLASH);
    Node right = stm.parse(ENTITY);
    if (left == null || right == null) {
      throw new LessException(error(GENERAL).arg0("Shorthand pattern matched but failed to complete parse"));
    }
    return new Shorthand(left, right);
  }

}
