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

import static com.squarespace.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.less.model.Operator.AND;
import static com.squarespace.less.model.Operator.EQUAL;
import static com.squarespace.less.parse.Parselets.CONDITION_SUB;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.core.Constants;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operator;


/**
 * Parse one or more conditions that make up a guard clause.
 */
public class ConditionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Condition cond = parseCondition(stm);
    stm.skipWs();
    while (stm.matchAnd()) {
      Condition sub = parseCondition(stm);
      cond = new Condition(AND, cond, sub, false);
      stm.skipWs();
    }
    return cond;
  }

  private Condition parseCondition(LessStream stm) throws LessException {
    stm.skipWs();
    boolean negate = stm.matchNot();

    Condition res = null;
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      throw stm.parseError(new LessException(expected("left parenthesis '(' to start guard condition")));
    }

    Node left = parseValue(stm);
    if (left == null) {
      throw stm.parseError(new LessException(expected("condition value")));
    }

    stm.skipWs();
    if (stm.matchBoolOperator()) {
      Operator op = Operator.fromString(stm.token());
      Node right = parseValue(stm);
      if (right != null) {
        res = new Condition(op, left, right, negate);

      } else {
        throw stm.parseError(new LessException(expected("expression")));
      }
    } else {
      res = new Condition(EQUAL, left, Constants.TRUE, negate);
    }

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw stm.parseError(new LessException(expected("right parenthesis ')' to end guard condition")));
    }
    return res;
  }

  private Node parseValue(LessStream stm) throws LessException {
    return stm.parse(CONDITION_SUB);
  }

}
