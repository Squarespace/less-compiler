package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.MULTIPLICATION;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Operation;
import com.squarespace.v6.template.less.model.Operator;


public class AdditionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node operand0 = stm.parse(MULTIPLICATION);
    if (operand0 == null) {
      return null;
    }

    // If no operator can be parsed, we return the operand as the result.
    Node operation = operand0;
    while (true) {
      Operator op = parseOperator(stm);
      if (op == null) {
        break;
      }
      Node operand1 = stm.parse(MULTIPLICATION);
      if (operand1 == null) {
        break;
      }
      operation = new Operation(op, operation, operand1);
    }
    return operation;
  }

  /**
   * Parse a single-character operator, avoiding treating bare positive/negative
   * numbers as an operation.
   */
  private Operator parseOperator(LessStream stm) {
    stm.skipWs();
    Operator op = Operator.fromChar(stm.peek());
    if (op != Operator.ADD && op != Operator.SUBTRACT) {
      return null;
    }
    if (CharClass.whitespace(stm.peek(1)) || !CharClass.whitespace(stm.peek(-1))) {
      stm.seek1();
      return op;
    }
    return null;
  }
  
}
