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

import static com.squarespace.less.parse.Parselets.EXPRESSION_SUB;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.Node;


/**
 * Parse a space-delimited list of entities.
 */
public class ExpressionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node first = stm.parse(EXPRESSION_SUB);
    if (first == null) {
      return null;
    }

    // Check if a 2nd entity exists. If not, just return the first.
    Node next = stm.parse(EXPRESSION_SUB);
    if (next == null) {
      return first;
    }

    // Two or more exist, create a wrapper.
    List<Node> entities = new ArrayList<>(4);
    entities.add(first);
    do {
      entities.add(next);
      next = stm.parse(EXPRESSION_SUB);
    } while (next != null);
    return new Expression(entities);
  }

}
