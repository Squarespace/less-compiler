package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.SyntaxErrorType.EXPECTED_MISC;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;


public class SubParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      return null;
    }
    
    Node node = stm.parse(EXPRESSION);

    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw new LessException(error(EXPECTED_MISC).arg0(')').arg1(stm.remainder()));
    }
    return node;
  }

}
