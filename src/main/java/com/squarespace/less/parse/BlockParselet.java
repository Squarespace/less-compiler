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

import static com.squarespace.less.parse.Parselets.PRIMARY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;


/**
 * Handles parsing of '{' PRIMARY '}' sequence for all block types (ruleset, media, etc).
 */
public class BlockParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    stm.skipWs();
    if (stm.peek() != Chars.LEFT_CURLY_BRACKET) {
      return null;
    }

    Mark mark = stm.mark();
    stm.seekOpenSpace();

    Block block = (Block)stm.parse(PRIMARY);
    stm.skipEmpty();

    if (stm.peek() != Chars.RIGHT_CURLY_BRACKET) {
      stm.restore(mark);
      return null;
    }

    stm.seekOpenSpace();
    return block;
  }

}
