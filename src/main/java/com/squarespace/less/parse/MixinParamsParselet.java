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
import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.EXPRESSION;
import static com.squarespace.less.parse.Parselets.MIXIN_PARAMETER;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Parameter;
import com.squarespace.less.model.Variable;


public class MixinParamsParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    int[] mark = stm.mark();
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      stm.restore(mark);
      stm.popMark();
      return null;
    }

    MixinParams params = new MixinParams();
    do {
      stm.parse(COMMENT);
      if (matchVariadic(stm)) {
        stm.seek(3);
        params.add(stm.context().nodeBuilder().buildParameter(null, true));
        break;
      }

      Parameter param = parseParam(stm);
      if (param == null) {
        break;
      }
      params.add(param);

      stm.skipWs();
      char ch = stm.peek();
      if (ch != Chars.COMMA && ch != Chars.SEMICOLON) {
        break;
      }
      stm.seek1();

    } while (true);

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      stm.restore(mark);
      stm.popMark();
      return null;
    }
    stm.popMark();
    return params;
  }

  private Parameter parseParam(LessStream stm) throws LessException {
    Node temp = stm.parse(MIXIN_PARAMETER);
    if (temp == null) {
      return null;
    }
    if (!(temp instanceof Variable)) {
      return stm.context().nodeBuilder().buildParameter(null, temp);
    }

    Variable var = (Variable)temp;

    stm.skipWs();
    if (stm.seekIf(Chars.COLON)) {
      stm.skipWs();
      Node value = stm.parse(EXPRESSION);
      if (value == null) {
        throw stm.parseError(new LessException(expected("an expression")));
      }
      return stm.context().nodeBuilder().buildParameter(var.name(), value);

    } else if (matchVariadic(stm)) {
      stm.seek(3);
      return stm.context().nodeBuilder().buildParameter(var.name(), true);
    }

    return stm.context().nodeBuilder().buildParameter(var.name());
  }

  private boolean matchVariadic(LessStream stm) throws LessException {
    return stm.peek() == Chars.PERIOD && stm.peek(1) == Chars.PERIOD && stm.peek(2) == Chars.PERIOD;
  }

}
