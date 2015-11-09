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

import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.parse.Recognizers.Recognizer;


/**
 * Slimmed down version of {@link Stream} for quick parsing jobs.
 * This maintains less state, e.g. doesn't track character and
 * line offsets, so it can be reset more efficiently.
 */
public abstract class LightweightStream {

  /**
   * Current string being parsed.
   */
  protected String raw;

  /**
   * Current character position in the string.
   */
  protected int index;

  /**
   * Length of the string.
   */
  protected int length;

  /**
   * Most recently parsed token. This value should only be used if the
   * {@link LightweightStream#match(Recognizer)} method returns true.
   */
  protected String token;

  /**
   * Initializes the parser to parse the given raw string.
   */
  protected void init(String str) {
    this.raw = str;
    this.index = 0;
    this.length = str.length();
  }

  /**
   * Match the recognizer at the current position in the string. If it matches,
   * we capture the token covered by the range, advance the current position and
   * return true.  Otherwise we leave the position unchanged and return false.
   *
   * Caller should only use the {@code token} variable if this method returns true.
   */
  protected boolean match(Recognizer pattern) {
    int pos = pattern.match(raw, index, length);
    if (pos > index) {
      // Match succeeded since it advanced past the current position.
      token = raw.substring(index, pos);
      index = pos;
      return true;
    }
    return false;
  }

  /**
   * Peek at the character at the current position. Return EOF if the position
   * is out of range.
   */
  protected char peek() {
    return (index >= length) ? Chars.EOF : raw.charAt(index);
  }

  /**
   * Peek at the character at the given offset from the current position. Return
   * EOF if the position is out of range.
   */
  protected char peek(int offset) {
    int pos = index + offset;
    return (pos < 0 || pos >= length) ? Chars.EOF : raw.charAt(pos);
  }

  /**
   * Advance the current position by 1 character.
   */
  protected void seek1() {
    index++;
  }

  /**
   * Advance the current position by N characters.
   */
  protected char seek(int offset) {
    index = Math.min(length, index + offset);
    return peek();
  }

  /**
   * If the character at the current position equals {@code ch} then advance
   * the pointer 1 position and return true.  Otherwise leave the position
   * unchanged and return false.
   */
  protected boolean seekIf(char ch) {
    if (peek() == ch) {
      seek1();
      return true;
    }
    return false;
  }

  /**
   * Skip over all whitespace characters.
   */
  protected int skipWs() {
    int start = index;
    while (index < length) {
      char curr = raw.charAt(index);
      if (!CharClass.whitespace(curr)) {
        break;
      }
      index++;
    }
    return index - start;
  }

}
