package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.OPERAND;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Operator;


public class MultiplicationParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node operand0 = stm.parse(OPERAND);
    if (operand0 == null) {
      return null;
    }

    Node operation = operand0;
    while (true) {
      stm.skipWs();
      
      Operator op = Operator.fromChar(stm.peek());
      if (op != Operator.MULTIPLY && op != Operator.DIVIDE) {
        break;
      }
      
      // Avoid treating a comment as the start of a divide operation..
      char ch = stm.peek(1);
      if (ch == Chars.ASTERISK || ch == Chars.SLASH) {
        return operand0;
      }
      stm.seek1();
      
      Node operand1 = stm.parse(OPERAND);
      if (operand1 == null) {
        break;
      }
      operation = new Operation(op, operation, operand1);
    }

    return operation;
  }

}
