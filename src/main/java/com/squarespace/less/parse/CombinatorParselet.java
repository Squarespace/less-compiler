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

import static com.squarespace.less.core.CharClass.combinator;
import static com.squarespace.less.core.CharClass.whitespace;
import static com.squarespace.less.model.CombinatorType.DESC;
import static com.squarespace.less.model.CombinatorType.fromChar;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Node;


/**
 * Parses a single combinator character.
 */
public class CombinatorParselet implements Parselet {

  private final boolean selectorStart;

  public CombinatorParselet(boolean selectorStart) {
    this.selectorStart = selectorStart;
  }

  @Override
  public Node parse(LessStream stm) throws LessException {
    char prev = stm.peek(-1);
    int skipped = stm.skipWs();
    char ch = stm.peek();

    if (combinator(ch)) {
      stm.seek1();
      return new Combinator(fromChar(ch));

    } else if (!selectorStart && (skipped > 0 || whitespace(prev))) {
      return new Combinator(DESC);
    }
    return null;
  }

}
