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

import static com.squarespace.less.core.SyntaxErrorMaker.selectorEndExtend;
import static com.squarespace.less.core.SyntaxErrorMaker.selectorEndGuard;
import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.ENTITY;
import static com.squarespace.less.parse.Parselets.EXTEND_OR_ELEMENT;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.SelectorPart;
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
        selector.add(new ValueElement(value));
        return selector;
      }
      return null;
    }

    LessContext context = stm.context();
    Node node = null;
    boolean extended = false;
    while (true) {

      // Note: this quietly eats combinator characters not followed by a valid
      // element. The combinator character will be parsed and when no
      // element is found, we simply return null. This currently replicates the
      // behavior of the upstream less.js parser.

      // Parse a combinator character. The start of the selector has
      // slightly different logic.
      Combinator combinator = null;
      if (node == null) {
        combinator = (Combinator) stm.parse(Parselets.COMBINATOR_START);
      } else {
        combinator = (Combinator) stm.parse(Parselets.COMBINATOR);
      }

      // Parse an extend list or element. We attempt parsing an extend
      // first, since its prefix ":extend" looks like a pseudo-element.
      stm.skipWs();
      node = stm.parse(EXTEND_OR_ELEMENT);
      if (node == null) {
        break;
      }

      // If we parsed a valid combinator, add it.
      if (combinator != null) {
        selector = initSelector(context, selector);
        selector.add(combinator);
      }

      // Ensure that if we're adding an element / combinator that it doesn't
      // come after an extend list.  Extend is always the last part of
      // a selector.
      if (node instanceof SelectorPart) {
        if (extended) {
          throw stm.parseError(new LessException(selectorEndExtend()));
        }
        selector = initSelector(context, selector);
        selector.add((SelectorPart)node);

      } else if (node instanceof ExtendList) {
        selector = initSelector(context, selector);
        selector.extendList((ExtendList)node);
        extended = true;
      }

      // Eat comments and whitespace
      stm.parse(COMMENT);
      stm.skipWs();

      // Selector guards are optional. We attempt to parse one and attach
      // it to the selector.
      Guard guard = (Guard)stm.parse(Parselets.RULESET_GUARD);
      if (guard != null) {
        selector = initSelector(context, selector);
        selector.guard(guard);
      }

      stm.skipWs();
      if (CharClass.selectorEnd(stm.peek())) {
        break;
      }

      if (guard != null) {
        throw stm.parseError(new LessException(selectorEndGuard()));
      }
    }

    return selector;
  }

  private Selector initSelector(LessContext context, Selector selector) {
    return selector == null ? context.nodeBuilder().buildSelector() : selector;
  }
}
