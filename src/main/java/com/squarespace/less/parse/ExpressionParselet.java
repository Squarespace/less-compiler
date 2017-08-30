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
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.Node;


/**
 * Parse a space-delimited list of entities.
 */
public class ExpressionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = stm.parse(EXPRESSION_SUB);
    if (node == null) {
      return null;
    }

    List<Node> entities = new ArrayList<>();
    do {
      entities.add(node);

      // Peek for '/' delimiter here.
      stm.skipWs();
      if (stm.peek() == Chars.SLASH && stm.peek(1) != Chars.ASTERISK) {
        stm.seek1();
        entities.add(new Anonymous(Character.toString(Chars.SLASH)));
      }

      node = stm.parse(EXPRESSION_SUB);
    } while (node != null);
    return entities.size() == 1 ? entities.get(0) : new Expression(entities);
  }

}
