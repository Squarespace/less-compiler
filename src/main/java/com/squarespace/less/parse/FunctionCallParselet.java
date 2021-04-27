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

import static com.squarespace.less.core.CharClass.CLASSIFIER;
import static com.squarespace.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.less.parse.Parselets.FUNCTION_CALL_ARGS;
import static com.squarespace.less.parse.Parselets.FUNCTION_CALL_SUB;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Url;


/**
 * Parse all forms which look like function calls.
 */
public class FunctionCallParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark position = stm.mark();
    if (!CLASSIFIER.callStart(stm.peek()) || !stm.matchCallName()) {
      return null;
    }

    String name = stm.token();
    String nameLC = name.toLowerCase();
    if (nameLC.equals("url")) {
      return parseUrl(stm);

    } else if (nameLC.equals("alpha")) {
      // Special handling for IE's alpha function.
      Node result = stm.parse(Parselets.ALPHA);
      if (result != null) {
        return result;
      }
      // Fall through, assuming the built-in alpha function.

    }

    // Use the lowercase version of the name to match less.js. CSS is case-insensitive
    // within the ASCII range.
    FunctionCall call = new FunctionCall(nameLC);
    ExpressionList args = parseArgs(stm);
    List<Node> expressions = args.expressions();
    int size = expressions.size();
    for (int i = 0; i < size; i++) {
      Node arg = expressions.get(i);
      call.add(arg);
    }

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      stm.restore(position);
      return null;
    }
    return call;
  }


  private ExpressionList parseArgs(LessStream stm) throws LessException {
    ExpressionList args = new ExpressionList();
    while (true) {
      Node value = stm.parse(FUNCTION_CALL_ARGS);
      if (value != null) {
        args.add(value);
      }

      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
    }
    return args;
  }

  public static Node parseUrl(LessStream stm) throws LessException {
    stm.skipWs();
    Node value = stm.parse(FUNCTION_CALL_SUB);
    if (value == null) {
      int start = stm.position();
      char ch = stm.peek();
      while (ch != Chars.RIGHT_PARENTHESIS && ch != Chars.EOF) {
        stm.seek(1);
        ch = stm.peek();
      }
      String url = stm.raw().substring(start, stm.position());
      value = new Anonymous(url.trim());
    }
    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw stm.parseError(new LessException(expected("right parenthesis ')' to end url")));
    }
    return new Url(value);
  }

}
