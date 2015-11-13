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

import com.squarespace.less.core.CharPattern;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;


/**
 * Parse both single line '//' and block '/*' comments.
 */
public class CommentParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    return parseComment(stm, false);
  }

  protected static Node parseComment(LessStream stm, boolean ruleLevel) {
    char ch = stm.peek();

    // Check for a comment start sequence, "//" or "/*".
    if (ch != Chars.SLASH) {
      return null;
    }

    ch = stm.peek(1);
    boolean block = ch == Chars.ASTERISK;
    boolean comment = (ch == Chars.SLASH) || block;
    if (!comment) {
      return null;
    }

    // Skip over "//" or "/*".
    stm.seek(2);
    int start = stm.index;

    // A comment without an ending sequence ends with EOF.
    int end = stm.length;

    // Match a comment ending sequence, "\n" or "*/"
    CharPattern pattern = block ? Patterns.BLOCK_COMMENT_END : Patterns.LINE_COMMENT_END;
    if (stm.seek(pattern)) {
      end = stm.index - pattern.length();
    }

    return stm.context().nodeBuilder().buildComment(stm.raw.substring(start, end), block, ruleLevel);
  }

}
