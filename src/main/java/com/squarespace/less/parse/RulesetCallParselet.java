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

import static com.squarespace.less.core.Chars.LEFT_PARENTHESIS;
import static com.squarespace.less.core.Chars.RIGHT_PARENTHESIS;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;


/**
 * Parses a detached {@link Ruleset} call.
 */
public class RulesetCallParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (stm.peek() != Chars.AT_SIGN) {
      return null;
    }

    Mark mark = stm.mark();
    stm.seek1();
    if (stm.matchIdentifier() && stm.peek() == LEFT_PARENTHESIS && stm.peek(1) == RIGHT_PARENTHESIS) {
      String name = stm.token();
      stm.seek(2);
      if (RuleParselet.end(stm)) {
        return stm.context().nodeBuilder().buildVariable("@" + name, false, true);
      }
    }
    stm.restore(mark);
    return null;
  }

}
