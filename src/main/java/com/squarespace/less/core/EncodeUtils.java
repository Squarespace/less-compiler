package com.squarespace.less.core;

import static com.squarespace.less.core.CharClass.DIGIT;
import static com.squarespace.less.core.CharClass.ENCODE_URI;
import static com.squarespace.less.core.CharClass.ENCODE_URI_COMPONENT;
import static com.squarespace.less.core.CharClass.LOWERCASE;
import static com.squarespace.less.core.CharClass.UPPERCASE;


/**
 * Implements the encodeURI and encodeURIComponent functions, which are available
 * as built-ins for JavaScript, but which no efficient version could be found for
 * Java.  Simple, straightforward implementation following the V8 engine, with
 * one exception: this avoids raising an error for invalid Unicode chars, choosing
 * instead to ignore them.
 */
public class EncodeUtils {

  private static final int ENCODE_URI_CLASS = ENCODE_URI | LOWERCASE | UPPERCASE | DIGIT;

  private static final int ENCODE_URI_COMPONENT_CLASS = ENCODE_URI_COMPONENT | LOWERCASE | UPPERCASE | DIGIT;

  private EncodeUtils() {
  }

  private interface CharTest {
    boolean member(char ch);
  }

  private static final CharTest ENCODE_URI_TEST = new CharTest() {
    @Override
    public boolean member(char ch) {
      return CharClass.isMember(ch, ENCODE_URI_CLASS);
    }
  };

  private static final CharTest ENCODE_URI_COMPONENT_TEST = new CharTest() {
    @Override
    public boolean member(char ch) {
      return CharClass.isMember(ch, ENCODE_URI_COMPONENT_CLASS);
    }
  };

  private static final CharTest ESCAPE_TEST = new CharTest() {
    @Override
    public boolean member(char ch) {
      return CharClass.isMember(ch, ENCODE_URI_CLASS) && !CharClass.isMember(ch, CharClass.ESCAPE);
    }
  };

  /**
   * Implementation of JavaScript's encodeURI function.
   */
  public static String encodeURI(String uri) {
    return encodeChars(uri, ENCODE_URI_TEST);
  }

  /**
   * Implementation of JavaScripts encodeURIComponent function.
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

  private static void hexoctet(StringBuilder buf, int octet) {
    buf.append('%').append(Chars.hexchar(octet >> 4)).append(Chars.hexchar(octet & 0xF));
  }

}
