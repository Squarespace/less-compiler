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

import static com.squarespace.less.parse.Parselets.EXPRESSION;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;


/**
 * Parse a comma-delimited list of expressions.
 */
public class ExpressionListParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = stm.parse(EXPRESSION);
    if (node == null) {
      return null;
    }

    stm.skipWs();
    if (stm.peek() != Chars.COMMA) {
      return node;
    }

    List<Node> expressions = new ArrayList<>(4);
    do {
      expressions.add(node);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
      node = stm.parse(EXPRESSION);
    } while (node != null);
    return new ExpressionList(expressions);
  }

}
