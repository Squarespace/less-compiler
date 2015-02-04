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

import static com.squarespace.less.core.SyntaxErrorMaker.alphaUnitsInvalid;
import static com.squarespace.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.less.parse.Parselets.ALPHA_SUB;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Alpha;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;


public class AlphaParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.matchOpacity()) {
      return null;
    }

    Node value = stm.parse(ALPHA_SUB);
    if (value != null && value.type().equals(NodeType.DIMENSION)) {
      Dimension dim = (Dimension)value;
      if (dim.unit() != null) {
        throw stm.parseError(new LessException(alphaUnitsInvalid(dim)));
      }
    }
    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      if (value == null) {
        throw stm.parseError(new LessException(expected("expected a unit-less number or variable for alpha")));
      } else {
        throw stm.parseError(new LessException(expected("right parenthesis ')' to end alpha")));
      }
    }
    return new Alpha(value == null ? new Anonymous("") : value);
  }

}
