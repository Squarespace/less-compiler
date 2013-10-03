package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.COLOR;
import static com.squarespace.v6.template.less.parse.Parselets.COLOR_KEYWORD;
import static com.squarespace.v6.template.less.parse.Parselets.DIMENSION;
import static com.squarespace.v6.template.less.parse.Parselets.FUNCTION_CALL;
import static com.squarespace.v6.template.less.parse.Parselets.SUB;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Operation;
import com.squarespace.v6.template.less.model.Operator;


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
    Node node = stm.parse(SUB, DIMENSION, FUNCTION_CALL, COLOR_KEYWORD, COLOR, VARIABLE);
    return negate ? new Operation(Operator.MULTIPLY, new Dimension(-1, null), node) : node;
  }

}
