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

import static com.squarespace.less.core.SyntaxErrorMaker.general;
import static com.squarespace.less.parse.Parselets.ENTITY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Shorthand;


public class ShorthandParselet implements Parselet {

  private static final String PARSE_ERROR = "Shorthand pattern matched but failed to complete parse";

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.peekShorthand()) {
      return null;
    }
    int[] position = stm.mark();
    Node left = stm.parse(ENTITY);
    if (!stm.seekIf(Chars.SLASH)) {
      stm.restore(position);
      stm.popMark();
      return null;
    }
    Node right = stm.parse(ENTITY);
    if (left == null || right == null) {
      stm.popMark();
      throw stm.parseError(new LessException(general(PARSE_ERROR)));
    }
    stm.popMark();
    return new Shorthand(left, right);
  }

}
