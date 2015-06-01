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

import static com.squarespace.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.less.core.SyntaxErrorMaker.mixedDelimiters;
import static com.squarespace.less.parse.Parselets.MIXIN_CALL_ARG;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Variable;


public class MixinCallArgsParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      return null;
    }

    boolean delimSemicolon = false;
    boolean containsNamed = false;
    MixinCallArgs argsComma = new MixinCallArgs(Chars.COMMA);
    MixinCallArgs argsSemicolon = new MixinCallArgs(Chars.SEMICOLON);
    ExpressionList expressions = new ExpressionList();
    String name = null;
    Node node = null;

    while ((node = stm.parse(MIXIN_CALL_ARG)) != null) {
      String nameLoop = null;
      Node value = node;

      // If the last node parsed is a variable reference, check if it is a named argument.
      // If not, treat the variable as a pass-by-reference value.

      // Examples:   (@x, @y)      - both @x and @y are pass-by-reference
      //             (@x: 1, @y)   - @x is pass-by-value named argument, @y is pass-by-reference
      //
      if (node instanceof Variable) {
        Variable var = (Variable)node;
        Node temp = parseVarArg(stm);
        if (temp != null) {
          if (expressions.size() > 0) {
            if (delimSemicolon) {
              throw stm.parseError(new LessException(mixedDelimiters()));
            }
            containsNamed = true;
          }
          value = temp;
          nameLoop = name = var.name();
        }
      }

      expressions.add(value);
      argsComma.add(new Argument(nameLoop, value));

      stm.skipWs();
      if (stm.seekIf(Chars.COMMA)) {
        continue;
      }

      // Detect whether the arguments are semicolon delimited.
      if (stm.seekIf(Chars.SEMICOLON)) {
        delimSemicolon = true;

      } else if (!delimSemicolon) {
        continue;
      }

      // Handle semicolon-delimited arguments.
      if (containsNamed) {
        throw stm.parseError(new LessException(mixedDelimiters()));
      }
      if (expressions.size() > 1) {
        value = expressions;
      }

      argsSemicolon.add(new Argument(name, value));
      name = null;
      expressions = new ExpressionList();
      containsNamed = false;
    }

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw stm.parseError(new LessException(expected("right parenthesis ')' to end mixin call arguments")));
    }

    return delimSemicolon ? argsSemicolon : argsComma;
  }

  private Node parseVarArg(LessStream stm) throws LessException {
    if (!stm.seekIf(Chars.COLON)) {
      return null;
    }
    Node value = stm.parse(MIXIN_CALL_ARG);
    if (value == null) {
      throw stm.parseError(new LessException(expected("expression for named argument")));
    }
    return value;
  }

}
