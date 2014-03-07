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

import static com.squarespace.less.parse.Parselets.ENTITY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.Node;


/**
 * Parses a NAME '=' ENTITY sequence.
 */
public class AssignmentParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark position = stm.mark();
    if (!stm.matchWord()) {
      return null;
    }
    String name = stm.token();

    stm.skipWs();
    if (!stm.seekIf(Chars.EQUALS_SIGN)) {
      stm.restore(position);
      return null;
    }

    Node value = stm.parse(ENTITY);
    if (value == null) {
      stm.restore(position);
      return null;
    }

    return new Assignment(name, value);
  }

}
