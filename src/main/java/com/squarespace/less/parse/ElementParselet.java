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

import static com.squarespace.less.parse.Parselets.ELEMENT_SUB;
import static com.squarespace.less.parse.Parselets.QUOTED;
import static com.squarespace.less.parse.Parselets.VARIABLE_CURLY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.ValueElement;
import com.squarespace.less.model.Variable;


/**
 * Parses pairs of combinators and elements.
 */
public class ElementParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {

    // Note: this quietly eats combinator characters not followed by a valid
    // element. The combinator character will be parsed and when no
    // element is found, we simply return null. This currently replicates the
    // behavior of the upstream less.js parser.

    Combinator comb = parseCombinator(stm);
    stm.skipWs();
    char ch = stm.peek();

    if (stm.matchElement0()) {
      return new TextElement(comb, stm.token());

    } else if (stm.matchElement1()) {
      return new TextElement(comb, stm.token());

    } else if (ch == Chars.ASTERISK || ch == Chars.AMPERSAND) {
      stm.seek1();
      return new TextElement(comb, "" + ch);
    }

    Node elem = parseAttribute(stm, comb);
    if (elem != null) {
      return elem;
    }

    if (stm.matchElement2()) {
      return new TextElement(comb, stm.token());

    } else if (stm.matchElement3()) {
      return new TextElement(comb, stm.token());

    } else {
      Node var = stm.parse(VARIABLE_CURLY);
      if (var != null) {
        return new ValueElement(comb, (Variable)var);
      }
    }

    if (elem == null) {
      elem = parseSub(stm);
      if (elem != null) {
        return new ValueElement(comb, elem);
      }
    }
    return null;
  }

  /**
   * For example input selector:  > p[class~="foo"]
   * This method parses the section between the '[' and ']' characters.
   */
  private Element parseAttribute(LessStream stm, Combinator comb) throws LessException {
    if (!stm.seekIf(Chars.LEFT_SQUARE_BRACKET)) {
      return null;
    }

    Node key = null;
    if (stm.matchAttributeKey()) {
      key = new Anonymous(stm.token());
    } else {
      key = stm.parse(QUOTED);
    }
    if (key == null) {
      return null;
    }

    AttributeElement elem = new AttributeElement(comb);
    elem.add(key);
    if (stm.matchAttributeOp()) {
      Node oper = new Anonymous(stm.token());
      Node val = stm.parse(QUOTED);
      if (val == null && stm.matchIdentifier()) {
        val = new Anonymous(stm.token());
      }
      if (val != null) {
        elem.add(oper);
        elem.add(val);
      }
    }

    if (!stm.seekIf(Chars.RIGHT_SQUARE_BRACKET)) {
      return null;
    }
    return elem;
  }

  /**
   * Parse a single combinator character from the stream.
   */
  private Combinator parseCombinator(LessStream stm) throws LessException {
    char prev = stm.peek(-1);
    int skipped = stm.skipWs();
    char ch = stm.peek();
    boolean useDesc = (prev == Chars.EOF || prev == Chars.COMMA || prev == Chars.LEFT_PARENTHESIS);
    if (CharClass.combinator(ch)) {
      stm.seek1();
      return Combinator.fromChar(ch);

    } else if (skipped > 0 || CharClass.whitespace(prev) || useDesc) {
      return Combinator.DESC;
    }
    return null;
  }

  private Node parseSub(LessStream stm) throws LessException {
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      return null;
    }
    Node value = stm.parse(ELEMENT_SUB);
    stm.skipWs();
    if (value != null && stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      return new Paren(value);
    }
    return null;
  }

}
