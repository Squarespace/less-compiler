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

import static com.squarespace.less.core.SyntaxErrorMaker.selectorOneGuard;
import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.SELECTOR;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;


public class SelectorsParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = stm.parse(SELECTOR);
    if (node == null) {
      return null;
    }

    Selector selector;
    boolean hasGuard = false;
    Selectors group = new Selectors();
    while (node != null) {
      selector = (Selector)node;
      if (hasGuard && !group.isEmpty()) {
        throw stm.parseError(new LessException(selectorOneGuard()));
      }

      hasGuard = selector.hasGuard();
      group.add(selector);

      stm.parse(COMMENT);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
      stm.parse(COMMENT);
      stm.skipWs();
      node = stm.parse(SELECTOR);
    }

    return group;
  }

}
