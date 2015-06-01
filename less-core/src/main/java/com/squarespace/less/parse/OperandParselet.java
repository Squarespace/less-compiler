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
