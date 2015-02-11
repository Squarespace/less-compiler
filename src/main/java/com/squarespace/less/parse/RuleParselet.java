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

import static com.squarespace.less.parse.Parselets.BLOCK;
import static com.squarespace.less.parse.Parselets.EXPRESSION_LIST;
import static com.squarespace.less.parse.Parselets.RULE_KEY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Variable;


public class RuleParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!CharClass.ruleStart(stm.peek())) {
      return null;
    }

    Mark ruleMark = stm.mark();
    Node key = parseKey(stm);
    if (key == null) {
      stm.restore(ruleMark);
      return null;
    }

    String name = null;
    if (key instanceof Property) {
      name = ((Property)key).name();
    } else {
      name = ((Variable)key).name();
    }
    Node value = null;
    stm.skipWs();

    Mark valueMark = stm.mark();

    // The 'font' rule has a special shorthand syntax which resembles
    // dimension division, but must be left unevaluated. We switch into
    // strict math mode for the entire rule.
    if (name.equals("font")) {
      stm.setRequireStrictMath(true);
    }

    // Try to parse a detached ruleset.
    value = stm.parse(BLOCK);

    // Fall back to parsing a normal expression list.
    if (value == null) {
      value = stm.parse(EXPRESSION_LIST);
      if (value != null) {
        // Flatten lists of length 1, simplifying the tree.
        ExpressionList expn = (ExpressionList)value;
        if (expn.size() == 1) {
          value = expn.expressions().get(0);
        }
      }
    }

    stm.setRequireStrictMath(false);

    stm.skipWs();
    boolean important = important(stm);

    // If we didn't read a full rule, back up and try to parse an opaque value.
    if (!endPeek(stm)) {
      important = false;
      stm.restore(valueMark);
      if (name.charAt(0) != Chars.AT_SIGN && stm.matchAnonRuleValue()) {
        value = new Anonymous(stm.token().trim());
      }
    } else if (value == null) {
      value = new Anonymous();
    }

    // Only emit a rule if we've parsed a value and found the rule ending.
    if (value != null && end(stm)) {
      if (key instanceof Variable) {
        // Note that !important is ingored for definitions.
        Definition def = stm.context().nodeBuilder().buildDefinition(name, value);
        def.fileName(stm.fileName());
        return def;

      } else {
        Rule rule = stm.context().nodeBuilder().buildRule((Property)key, value, important);
        rule.fileName(stm.fileName());
        return rule;
      }
    }

    stm.restore(ruleMark);
    return null;
  }

  private Node parseKey(LessStream stm) throws LessException {
    Node key = stm.parse(RULE_KEY);
    stm.skipWs();
    if (stm.seekIf(Chars.COLON)) {
      return key;
    }
    return null;
  }

  private boolean important(LessStream stm) {
    return (stm.peek() == Chars.EXCLAMATION_MARK && stm.matchImportant());
  }

  static boolean endPeek(LessStream stm) {
    stm.skipWs();
    char ch = stm.peek();
    return ch == Chars.SEMICOLON || ch == Chars.RIGHT_CURLY_BRACKET || ch == Chars.EOF;
  }

  static boolean end(LessStream stm) {
    stm.skipWs();
    switch (stm.peek()) {

      case Chars.SEMICOLON:
        stm.seek1();
        return true;

      case Chars.EOF:
      case Chars.RIGHT_CURLY_BRACKET:
        return true;

      default:
        break;
    }
    return false;
  }

}
