/**
 * Copyright, 2015, Squarespace, Inc.
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

import static com.squarespace.less.core.SyntaxErrorMaker.extendMissingTarget;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.BaseNode;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Extend;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;


/**
 * Parses a selector extend element or rule.
 */
public class ExtendParselet implements Parselet {

  /**
   * Indicates this parser looks for rule level extend lists.
   */
  private final boolean ruleLevel;

  public ExtendParselet(boolean atRuleLevel) {
    this.ruleLevel = atRuleLevel;
  }

  /**
   * @see Parselet#parse(LessStream)
   */
  @Override
  public Node parse(LessStream stm) throws LessException {

    // Since this will get called many times by the parent Parselet,
    // we peek for the start of the sequence "&*:".
    // Most of the time we'll fail to parse an extend, so we want
    // abort efficiently.

    int start = 0;
    if (ruleLevel) {
      if (stm.peek() != Chars.AMPERSAND) {
        return null;
      }
      start++;
    }

    if (stm.peek(start) != Chars.COLON) {
      return null;
    }

    // We have higher confidence we're parsing an extend, so mark
    // the position and do the deeper match.
    Mark mark = stm.mark();
    if (ruleLevel) {
      // Skip over the ampersand
      stm.seek1();
    }

    // Check if we match the ":extend(" sequence, including the left paren.
    if (!stm.matchExtend()) {
      stm.restore(mark);
      return null;
    }

    // Parse 1..N extend expressions, each consisting of a selector and
    // optional "all" keyword.  Append each to the extend list.
    ExtendList extendList = new ExtendList(ruleLevel);
    do {
      Extend node = parseExtend(stm);
      extendList.add(node);
    } while (stm.seekIf(Chars.COMMA));

    // Right paren terminates the extend list.
    if (stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      return extendList;
    }

    // That parse didn't go well, so back up and abort.
    stm.restore(mark);
    return null;
  }

  /**
   * Parse a single extend, which consists of a selector terminated by optional
   * "all" keyword.
   */
  private Extend parseExtend(LessStream stm) throws LessException {
    Selector selector = null;
    stm.skipWs();
    while (true) {
      // Check if the "all" keyword ends the element sequence.
      if (stm.matchExtendAll()) {
        if (selector == null) {
          throw stm.parseError(new LessException(extendMissingTarget()));
        }
        return newExtend(selector, true);
      }

      // Parse an element with optional combinator.
      BaseNode elem = (BaseNode)stm.parse(Parselets.ELEMENT);

      // No element was parsed, so we're done.
      if (elem == null) {
        if (selector == null) {
          throw stm.parseError(new LessException(extendMissingTarget()));
        }
        return newExtend(selector, false);
      }

      // Parsed an element, add it to the selector.
      if (selector == null) {
        selector = stm.context().nodeBuilder().buildSelector();
        selector.copyPosition(elem);
      }
      selector.add((Element)elem);
      stm.skipWs();
    }
  }

  private Extend newExtend(Selector selector, boolean matchAll) {
    Extend extend = new Extend(selector, matchAll);
    extend.copyPosition(selector);
    return extend;
  }

}
