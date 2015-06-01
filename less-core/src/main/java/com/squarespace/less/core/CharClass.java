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


/**
 * Custom character classifications for CSS/LESS. Builds an internal table which
 * contains flags for each character from 0x00 through 0x80. Used in place of
 * a switch() or if/else block for each character class.
 */
public class CharClass {

  // Character class bits that can be set/cleared in a bit mask

  public static final int DIGIT = 1 << 0;

  public static final int LOWERCASE = 1 << 1;

  public static final int UPPERCASE = 1 << 2;

  public static final int KEYWORD_START = 1 << 3;

  public static final int DIMENSION_START = 1 << 4;

  public static final int NONASCII = 1 << 5;

  public static final int NONPRINTABLE = 1 << 6;

  public static final int CALL_START = 1 << 7;

  public static final int COMBINATOR = 1 << 8;

  public static final int SELECTOR_END = 1 << 9;

  public static final int PROPERTY_START = 1 << 10;

  public static final int VARIABLE_START = 1 << 11;

  public static final int ENCODE_URI = 1 << 12;

  public static final int ENCODE_URI_COMPONENT = 1 << 13;

  public static final int ESCAPE = 1 << 14;

  public static final int HEXDIGIT = 1 << 15;

  public static final int DASH = 1 << 16;

  public static final int UNDERSCORE = 1 << 17;

  public static final int PERIOD = 1 << 18;

  /**
   * The characters we care about all live below this limit.
   */
  private static final int LIMIT = 0x80;

  /**
   * Table of character codes, where the value is a bit mask indicating which
   * classes that character belongs to.
   */
  private static final int[] CHARACTER_CLASSES = new int[LIMIT];

  static {
    for (int i = 0; i < LIMIT; i++) {
      CHARACTER_CLASSES[i] = classify((char)i);
    }
  }

  public static boolean callStart(char ch) {
    return isMember(ch, CALL_START);
  }

  public static boolean combinator(char ch) {
    return isMember(ch, COMBINATOR);
  }

  public static boolean digit(char ch) {
    return isMember(ch, DIGIT);
  }

  public static boolean dimensionStart(char ch) {
    return isMember(ch, DIMENSION_START);
  }

  public static boolean keywordStart(char ch) {
    return isMember(ch, KEYWORD_START);
  }

  public static boolean nonprintable(char ch) {
    return isMember(ch, NONPRINTABLE);
  }

  public static boolean propertyStart(char ch) {
    return isMember(ch, PROPERTY_START);
  }

  public static boolean ruleStart(char ch) {
    return isMember(ch, PROPERTY_START | VARIABLE_START);
  }

  public static boolean selectorEnd(char ch) {
    return isMember(ch, SELECTOR_END);
  }

  public static boolean skippable(char ch) {
    return ch == ';' || whitespace(ch);
  }

  public static boolean uppercase(char ch) {
    return isMember(ch, UPPERCASE);
  }

  /**
   * V8 JavaScript engine's whitespace ranges.
   */
  public static boolean whitespace(char ch) {
    return (ch == ' ')
        || (ch >= '\t' && ch <= '\r')
        || (ch == '\u00a0')   // nbsp
        || (ch == '\u1680')   // ogham space
        || (ch == '\u180e')   // mongolian vowel separator
        || (ch >= '\u2000' && ch <= '\u200a')   // [en space, ..., hair space]
        || (ch == '\u2028')   // line separator
        || (ch == '\u2029')   // paragraph separator
        || (ch == '\u202f')   // narrow nbsp
        || (ch == '\u205f')   // medium mathematical space
        || (ch == '\u3000')   // ideographic space
        || (ch == '\ufeff');  // byte order mark
  }

  public static boolean isMember(char ch, int cls) {
    return (ch >= LIMIT) ? false : (CHARACTER_CLASSES[ch] & cls) > 0;
  }

  /**
   * Table used to generate static char classification table.  This is only called
   * for each character < LIMIT once to build the lookup table.
   */
  private static int classify(char ch) {
    switch (ch) {
      case '\u0000':
      case '\u0001':
      case '\u0002':
      case '\u0003':
      case '\u0004':
      case '\u0005':
      case '\u0006':
      case '\u0007':
      case '\u0008':
        return NONPRINTABLE;

      case '\u000E':
      case '\u000F':
      case '\u0010':
      case '\u0011':
      case '\u0012':
      case '\u0013':
      case '\u0014':
      case '\u0015':
      case '\u0016':
      case '\u0017':
      case '\u0018':
      case '\u0019':
      case '\u001A':
      case '\u001B':
      case '\u001C':
      case '\u001D':
      case '\u001E':
      case '\u001F':
        return NONPRINTABLE;

      case '!':
        return ENCODE_URI | ENCODE_URI_COMPONENT;

      case '#':
        return ENCODE_URI | ESCAPE;

      case '$':
        return ENCODE_URI;

      case '%':
        return CALL_START;

      case '&':
        return ENCODE_URI;

      case '\'':
        return ENCODE_URI | ENCODE_URI_COMPONENT;

      case '(':
        return ENCODE_URI | ENCODE_URI_COMPONENT | ESCAPE;

      case ')':
        return SELECTOR_END | ENCODE_URI | ENCODE_URI_COMPONENT | ESCAPE;

      case '*':
        return PROPERTY_START | ENCODE_URI | ENCODE_URI_COMPONENT;

      case '+':
        return DIMENSION_START | COMBINATOR | ENCODE_URI;

      case ',':
        return SELECTOR_END | ENCODE_URI;

      case '-':
        return DASH
             | CALL_START
             | DIMENSION_START
             | KEYWORD_START
             | PROPERTY_START
             | ENCODE_URI
             | ENCODE_URI_COMPONENT;

      case '.':
        return DIMENSION_START | ENCODE_URI | ENCODE_URI_COMPONENT;

      case '/':
        return ENCODE_URI;

      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        return DIGIT | DIMENSION_START | PROPERTY_START | HEXDIGIT;

      case ':':
        return ENCODE_URI | ESCAPE;

      case ';':
        return SELECTOR_END | ENCODE_URI | ESCAPE;

      case '=':
        return ENCODE_URI | ESCAPE;

      case '>':
        return COMBINATOR;

      case '?':
        return ENCODE_URI;

      case '@':
        return VARIABLE_START | ENCODE_URI;

      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
        return UPPERCASE | CALL_START | KEYWORD_START | HEXDIGIT;

      case 'G':
      case 'H':
      case 'I':
      case 'J':
      case 'K':
      case 'L':
      case 'M':
      case 'N':
      case 'O':
      case 'P':
      case 'Q':
      case 'R':
      case 'S':
      case 'T':
      case 'U':
      case 'V':
      case 'W':
      case 'X':
      case 'Y':
      case 'Z':
        return UPPERCASE | CALL_START | KEYWORD_START;

      case '_':
        return UNDERSCORE
             | CALL_START
             | KEYWORD_START
             | PROPERTY_START
             | ENCODE_URI
             | ENCODE_URI_COMPONENT;

      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
        return LOWERCASE | CALL_START | KEYWORD_START | PROPERTY_START | HEXDIGIT;

      case 'g':
      case 'h':
      case 'i':
      case 'j':
      case 'k':
      case 'l':
      case 'm':
      case 'n':
      case 'o':
      case 'p':
      case 'q':
      case 'r':
      case 's':
      case 't':
      case 'u':
      case 'v':
      case 'w':
      case 'x':
      case 'y':
      case 'z':
        return LOWERCASE | CALL_START | KEYWORD_START | PROPERTY_START;

      case '{':
        return SELECTOR_END;

      case '|':
        return COMBINATOR;

      case '}':
        return SELECTOR_END;

      case '~':
        return COMBINATOR | ENCODE_URI | ENCODE_URI_COMPONENT;

      default:
        break;
    }

    return (ch >= Chars.NO_BREAK_SPACE) ? NONASCII : 0;
  }

}
