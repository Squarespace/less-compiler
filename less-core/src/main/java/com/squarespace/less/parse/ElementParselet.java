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

import static com.squarespace.less.core.Chars.AMPERSAND;
import static com.squarespace.less.core.Chars.ASTERISK;
import static com.squarespace.less.core.Chars.LEFT_PARENTHESIS;
import static com.squarespace.less.core.Chars.LEFT_SQUARE_BRACKET;
import static com.squarespace.less.core.Chars.RIGHT_PARENTHESIS;
import static com.squarespace.less.core.Chars.RIGHT_SQUARE_BRACKET;
import static com.squarespace.less.parse.Parselets.ELEMENT_SUB;
import static com.squarespace.less.parse.Parselets.QUOTED;
import static com.squarespace.less.parse.Parselets.VARIABLE_CURLY;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.SelectorPart;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.ValueElement;
import com.squarespace.less.model.Variable;
import com.squarespace.less.model.WildcardElement;


/**
 * Parses various element {@link SelectorPart}s.
 */
public class ElementParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {

    stm.skipWs();
    char ch = stm.peek();

    // The parsing logic below runs a series of tests of the current
    // stream position. When one fails to match it falls back to
    // the next pattern.
    //
    // In order of preference, the patterns attempted are:
    //
    //  element0   - decimal percent
    //  element1   - main class/id pattern
    //  asterisk   - universal selector
    //  ampersand  - wild card selector
    //  attribute  - rich attribute pattern
    //  element2   - parenthesis-wrapped text element
    //  element3   - class/id prefix to a variable element
    //  variable   - curly variable
    //  sub        - parenthesis-wrapped variable / selector

    if (stm.matchElement0()) {
      return new TextElement(stm.token());

    } else if (stm.matchElement1()) {
      return new TextElement(stm.token());

    } else if (ch == ASTERISK) {
      stm.seek1();
      return new TextElement(Character.toString(ch));

    } else if (ch == AMPERSAND) {
      stm.seek1();
      return new WildcardElement();
    }

    // If nothing is matched, attempt to parse an attribute element.
    SelectorPart elem = parseAttribute(stm);
    if (elem != null) {
      return elem;
    }

    if (stm.matchElement2()) {
      return new TextElement(stm.token());

    } else if (stm.matchElement3()) {
      return new TextElement(stm.token());

    } else {
      Node var = stm.parse(VARIABLE_CURLY);
      if (var != null) {
        return new ValueElement((Variable)var);
      }
    }

    if (elem == null) {
      Node node = parseSub(stm);
      if (node != null) {
        return new ValueElement(node);
      }
    }

    return null;
  }

  /**
   * For example input selector:  > p[class~="foo"]
   * This method parses the section between the '[' and ']' characters.
   */
  private SelectorPart parseAttribute(LessStream stm) throws LessException {
    if (!stm.seekIf(LEFT_SQUARE_BRACKET)) {
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

    AttributeElement elem = new AttributeElement();
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

    if (!stm.seekIf(RIGHT_SQUARE_BRACKET)) {
      return null;
    }
    return elem;
  }

  /**
   * Parses a parenthesis-wrapped variable or sub-selector.
   */
  private Node parseSub(LessStream stm) throws LessException {
    stm.skipWs();
    if (!stm.seekIf(LEFT_PARENTHESIS)) {
      return null;
    }
    Node value = stm.parse(ELEMENT_SUB);
    stm.skipWs();
    if (value != null && stm.seekIf(RIGHT_PARENTHESIS)) {
      return new Paren(value);
    }
    return null;
  }

}
