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

import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Variable;


public class VariableCurlyParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    boolean indirect = false;
    int pos = 0;
    if (stm.peek() != Chars.AT_SIGN) {
      return null;
    }
    pos++;

    if (stm.peek(pos) == Chars.AT_SIGN) {
      indirect = true;
      pos++;
    }

    if (stm.peek(pos) != Chars.LEFT_CURLY_BRACKET) {
      return null;
    }
    pos++;

    Mark mark = stm.mark();
    stm.seek(pos);
    if (!stm.matchIdentifier()) {
      stm.restore(mark);
      return null;
    }
    String token = '@' + stm.token();
    if (!stm.seekIf(Chars.RIGHT_CURLY_BRACKET)) {
      stm.restore(mark);
      return null;
    }
    return new Variable(indirect ? '@' + token : token, true);
  }

}
