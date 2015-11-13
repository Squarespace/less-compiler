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

package com.squarespace.less.core;

import static com.squarespace.less.core.CharClass.DIGIT;
import static com.squarespace.less.core.CharClass.ENCODE_URI;
import static com.squarespace.less.core.CharClass.ENCODE_URI_COMPONENT;
import static com.squarespace.less.core.CharClass.LOWERCASE;
import static com.squarespace.less.core.CharClass.UPPERCASE;


/**
 * Implements the encodeURI and encodeURIComponent functions, which are available
 * as built-ins for JavaScript, but which no efficient version could be found for
 * Java.
 *
 * Straightforward implementation following the V8 engine, with one exception:
 * this avoids raising an error for invalid Unicode character sequences, choosing
 * instead to ignore and skip over them.
 */
public class EncodeUtils {

  /**
   * Tests character membership for {@link #encodeURI(String)}
   */
  private static final CharTest ENCODE_URI_TEST = new CharTest() {
    @Override
    public boolean member(char ch) {
      return CharClass.isMember(ch, ENCODE_URI_CLASS);
    }
  };

  /**
   * Tests character membership for {@link #encodeURIComponent(String)}
   */
  private static final CharTest ENCODE_URI_COMPONENT_TEST = new CharTest() {
    @Override
    public boolean member(char ch) {
      return CharClass.isMember(ch, ENCODE_URI_COMPONENT_CLASS);
    }
  };

  /**
   * Tests character membership for {@link #escape(String)}
   */
  private static final CharTest ESCAPE_TEST = new CharTest() {
    @Override
    public boolean member(char ch) {
      return CharClass.isMember(ch, ENCODE_URI_CLASS) && !CharClass.isMember(ch, CharClass.ESCAPE);
    }
  };

  /**
   * Defines the character classes that are transformed by the
   * {@link #encodeURI(String)} method.
   */
  private static final int ENCODE_URI_CLASS = ENCODE_URI | LOWERCASE | UPPERCASE | DIGIT;

  /**
   * Defines the character classes that are transformed by the
   * {@link #encodeURIComponent(String)} method.
   */
  private static final int ENCODE_URI_COMPONENT_CLASS = ENCODE_URI_COMPONENT | LOWERCASE | UPPERCASE | DIGIT;

  private EncodeUtils() {
  }

  /**
   * Interface for testing a character's membership in a class.
   */
  private interface CharTest {
    boolean member(char ch);
  }

  /**
   * Implementation of JavaScript's encodeURI function.
   */
  public static String encodeURI(String uri) {
    return encodeChars(uri, ENCODE_URI_TEST);
  }

  /**
   * Implementation of JavaScript's encodeURIComponent function.
   */
  public static String encodeURIComponent(String uri) {
    return encodeChars(uri, ENCODE_URI_COMPONENT_TEST);
  }

  /**
   * Extends encodeURI with further character escapes.
   */
  public static String escape(String uri) {
    return encodeChars(uri, ESCAPE_TEST);
  }

  /**
   * Emits UTF-8 hex escape sequences for all characters which are not members of
   * the given character class. It ignores bad Unicode sequences.
   */
  private static String encodeChars(String uri, CharTest charTest) {
    StringBuilder buf = new StringBuilder();
    int size = uri.length();
    int i = 0;
    while (i < size) {
      char ch = uri.charAt(i);
      if (charTest.member(ch)) {
        buf.append(ch);
        i++;
        continue;

      } else if (ch <= 0x7F) {
        hexoctet(buf, ch);
        i++;
        continue;

      } else if (ch >= 0xDC00 && ch <= 0xDFFF) {
        // skip
        i++;
        continue;

      } else if (ch < 0xD800 || ch > 0xDBFF) {
        // Unicode single char
        // Emit UTF-8 escape sequence
        int x = (ch >> 12) & 0xF;
        int y = (ch >> 6) & 0x3F;
        int z = ch & 0x3F;
        if (ch <= 0x07FF) {
          hexoctet(buf, y + 192);
          hexoctet(buf, z + 128);
        } else {
          hexoctet(buf, x + 224);
          hexoctet(buf, y + 128);
          hexoctet(buf, z + 128);
        }
        i++;
        continue;
      }

      // Unicode surrogate pair
      i++;
      if (i == size) {
        break;
      }
      char ch2 = uri.charAt(i);
      if (ch2 < 0xDC00 || ch2 > 0xDFFF) {
        // skip
        continue;
      }

      // Emit UTF-8 escape sequence
      int u = ((ch >> 6) & 0xF) + 1;
      int w = (ch >> 2) & 0xF;
      int x = ch & 3;
      int y = (ch2 >> 6) & 0xF;
      int z = ch2 & 0x3F;
      hexoctet(buf, (u >> 2) + 240);
      hexoctet(buf, (((u & 3) << 4) | w) + 128);
      hexoctet(buf, ((x << 4) | y) + 128);
      hexoctet(buf, z + 128);
      i++;
    }
    return buf.toString();
  }

  /**
   * Appends a hexadecimal encoding of the character code.
   */
  private static void hexoctet(StringBuilder buf, int octet) {
    buf.append('%').append(Chars.hexchar(octet >> 4)).append(Chars.hexchar(octet & 0xF));
  }

}
