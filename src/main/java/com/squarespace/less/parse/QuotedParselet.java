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

import static com.squarespace.less.core.SyntaxErrorMaker.quotedBareLF;
import static com.squarespace.less.parse.Parselets.VARIABLE_CURLY;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;


public class QuotedParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    stm.skipWs();
    int offset = 0;
    boolean escaped = false;
    char ch = stm.peek();
    if (ch == Chars.TILDE) {
      escaped = true;
      offset++;
    }

    // Locate the string delimiter.
    char delim = stm.peek(offset);
    if (delim != Chars.APOSTROPHE && delim != Chars.QUOTATION_MARK) {
      return null;
    }

    // We have the beginning of a string.
    stm.seek(offset + 1);
    List<Node> parts = new ArrayList<>();
    StringBuilder buf = new StringBuilder();

    while (stm.index < stm.length) {
      ch = stm.peek();

      // If we see an @ symbol embedded into a quoted string, we have to check if it is a
      // valid variable reference.  We save the current position within the stream and
      // attempt to parse out the reference.  If it fails, we restore the position and
      // continue processing.
      if (ch == Chars.AT_SIGN) {
        Node ref = stm.parse(VARIABLE_CURLY);
        if (ref != null) {
          if (buf.length() > 0) {
            parts.add(new Anonymous(buf.toString()));
            buf = new StringBuilder();
          }
          parts.add(ref);
          continue;

        }
      }
      stm.seek1();

      // We've located end of string.
      if (ch == delim || ch == Chars.EOF) {
        break;
      }

      if (ch == Chars.LINE_FEED) {
        throw stm.parseError(new LessException(quotedBareLF()));
      }

      // We care about backslash only to avoid prematurely terminating the string. All
      // backslash-escaped sequences are left intact.

      if (ch != '\\') {
        buf.append(ch);
        continue;
      }

      // Collect the entire \" or \' sequence.
      buf.append(ch);
      ch = stm.peek();
      if (ch != Chars.EOF) {
        buf.append(ch);
        stm.seek1();
      }
    }

    if (buf.length() > 0) {
      parts.add(new Anonymous(buf.toString()));
    }
    return new Quoted(delim, escaped, parts);
  }
}
