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
import static com.squarespace.less.parse.Parselets.FONT_SUB;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;


/**
 * Parse a special value for a 'font' rule.
 */
public class FontParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    List<Node> expn = new ArrayList<>(2);
    Node node = stm.parse(FONT_SUB);
    while (node != null) {
      expn.add(node);

      stm.skipWs();
      if (stm.peek() == Chars.SLASH) {
        char next = stm.peek(1);
        // Ensure this isn't a start of COMMENT
        if (next != Chars.ASTERISK && next != Chars.SLASH) {
          stm.seek1();
          expn.add(new Anonymous("/"));
        }
      }
      node = stm.parse(FONT_SUB);
    }

//    while ((node = stm.parse(FONT_SUB)) != null) {
//      expn.add(node);
//    }

    ExpressionList value = new ExpressionList(new Expression(expn));
    stm.skipWs();
    if (stm.seekIf(Chars.COMMA)) {
      while ((node = stm.parse(EXPRESSION)) != null) {
        value.add(node);
        stm.skipWs();
        if (!stm.seekIf(Chars.COMMA)) {
          break;
        }
      }
    }
    return value;
  }

}
