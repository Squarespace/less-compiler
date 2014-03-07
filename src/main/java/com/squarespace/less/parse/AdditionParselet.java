/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.MULTIPLICATION;

import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Operator;


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
