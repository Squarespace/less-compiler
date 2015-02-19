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

import static com.squarespace.less.core.SyntaxErrorMaker.selectorEndGuard;
import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.ELEMENT;
import static com.squarespace.less.parse.Parselets.ENTITY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.ValueElement;


public class SelectorParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Selector selector = null;

    // Parse deprecated syntax (~"@{foo}")
    stm.skipWs();
    if (stm.peek() == Chars.LEFT_PARENTHESIS) {
      stm.seek1();
      Node value = stm.parse(ENTITY);
      stm.skipWs();
      if (stm.peek() == Chars.RIGHT_PARENTHESIS) {
        stm.seek1();
        selector = stm.context().nodeBuilder().buildSelector();
        selector.add(new ValueElement(Combinator.DESC, value));
        return selector;
      }
      return null;
    }

    Node elem = null;
    while ((elem = stm.parse(ELEMENT)) != null) {
      if (selector == null) {
        selector = stm.context().nodeBuilder().buildSelector();
      }
      selector.add((Element)elem);
      stm.parse(COMMENT);
      stm.skipWs();

      Guard guard = (Guard)stm.parse(Parselets.RULESET_GUARD);
      if (guard != null) {
        selector.guard(guard);
      }

      if (CharClass.selectorEnd(stm.peek())) {
        break;
      }

      if (guard != null) {
        throw stm.parseError(new LessException(selectorEndGuard()));
      }
    }
    return selector;
  }

}
