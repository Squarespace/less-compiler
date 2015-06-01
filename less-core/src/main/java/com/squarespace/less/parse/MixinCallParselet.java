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

import static com.squarespace.less.core.Chars.EOF;
import static com.squarespace.less.core.Chars.GREATER_THAN_SIGN;
import static com.squarespace.less.core.Chars.NUMBER_SIGN;
import static com.squarespace.less.core.Chars.PERIOD;
import static com.squarespace.less.core.Chars.RIGHT_CURLY_BRACKET;
import static com.squarespace.less.core.Chars.SEMICOLON;
import static com.squarespace.less.model.CombinatorType.CHILD;
import static com.squarespace.less.model.CombinatorType.DESC;
import static com.squarespace.less.parse.Parselets.MIXIN_CALL_ARGS;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.TextElement;


public class MixinCallParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    char ch = stm.peek();
    if (ch != PERIOD && ch != NUMBER_SIGN) {
      return null;
    }
    Mark mark = stm.mark();
    Selector selector = new Selector();
    Combinator combinator = null;
    while (stm.matchMixinName()) {
      if (combinator != null) {
        selector.add(combinator);
        combinator = null;
      }
      selector.add(new TextElement(stm.token()));

      int skipped = stm.skipWs();
      if (stm.peek() == GREATER_THAN_SIGN) {
        combinator = new Combinator(CHILD);
        stm.seek1();
      } else if (skipped > 0) {
        combinator = new Combinator(DESC);
      }
      stm.skipWs();
    }

    // If we failed to parse a valid selector, reset and bail out.
    if (selector.isEmpty()) {
      stm.restore(mark);
      return null;
    }

    MixinCallArgs args = (MixinCallArgs) stm.parse(MIXIN_CALL_ARGS);
    stm.skipWs();
    boolean important = stm.matchImportant();

    stm.skipWs();
    ch = stm.peek();

    MixinCall call = null;
    if (ch == SEMICOLON) {
      stm.seek1();
      call = stm.context().nodeBuilder().buildMixinCall(selector, args, important);

    } else if (ch == RIGHT_CURLY_BRACKET || ch == EOF) {
      call = stm.context().nodeBuilder().buildMixinCall(selector, args, important);
    }
    if (call != null) {
      call.fileName(stm.fileName());
      return call;
    }

    stm.restore(mark);
    return null;
  }


}
