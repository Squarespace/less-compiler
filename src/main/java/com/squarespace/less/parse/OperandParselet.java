package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.OPERAND_SUB;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Operator;


public class OperandParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    boolean negate = false;
    char ch0 = stm.peek();
    char ch1 = stm.peek(1);
    if (ch0 == '-' && (ch1 == Chars.AT_SIGN || ch1 == Chars.LEFT_PARENTHESIS)) {
      negate = true;
      stm.seek1();
    }
    Node node = stm.parse(OPERAND_SUB);
    return negate ? new Operation(Operator.MULTIPLY, node, new Dimension(-1, null)) : node;
  }

}
