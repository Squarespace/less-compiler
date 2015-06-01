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

    Node current = operand0;
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
      Operation operation = stm.context().nodeBuilder().buildOperation(op, current, operand1);
      operation.setSubExpression(stm.inParens());
      operation.setRequireStrictMath(stm.requireStrictMath());
      current = operation;
    }

    return current;
  }

}
