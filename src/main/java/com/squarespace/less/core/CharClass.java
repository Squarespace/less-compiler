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

import com.squarespace.less.match.CharClassifier;

/**
 * Custom character classifications for CSS/LESS. Builds an internal table which
 * contains flags for each character from 0x00 through 0x80. Used in place of
 * a switch() or if/else block for each character class.
 */
public class CharClass implements CharClassifier {

  // Character class bits that can be set/cleared in a bit mask

  public static final int DIGIT = 0x01;

  public static final int LOWERCASE = 0x02;

  public static final int UPPERCASE = 0x04;

  public static final int KEYWORD_START = 0x08;

  public static final int DIMENSION_START = 0x10;

  public static final int NONASCII = 0x20;

  public static final int NONPRINTABLE = 0x40;

  public static final int CALL_START = 0x80;

  public static final int COMBINATOR = 0x100;

  public static final int SELECTOR_END = 0x200;

  public static final int PROPERTY_START = 0x400;

  public static final int VARIABLE_START = 0x800;

  public static final int ENCODE_URI = 0x1000;

  public static final int ENCODE_URI_COMPONENT = 0x2000;

  public static final int ESCAPE = 0x4000;

  public static final int HEXDIGIT = 0x8000;

  public static final int DIRECTIVE = 0x10000;

  public static final int IDENTIFIER_START = 0x20000;

  public static final int IDENTIFIER = 0x40000;   //[\w-]

  public static final int PROPERTY = 0x80000;

  public static final int SHORTHAND = 0x100000;

  public static final int HEXWILD = 0x200000;

  public static final int WORD = 0x400000;

  public static final int PROGID_WORD = 0x800000;

  // Alias
  public static final int KEYWORD = IDENTIFIER;

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

  public static final CharClass CLASSIFIER = new CharClass();

  public boolean callStart(char ch) {
    return isMember(ch, CALL_START);
  }

  public boolean combinator(char ch) {
    return isMember(ch, COMBINATOR);
  }

  public boolean digit(char ch) {
    return isMember(ch, DIGIT);
  }

  public boolean dimensionStart(char ch) {
    return isMember(ch, DIMENSION_START);
  }

  public boolean keywordStart(char ch) {
    return isMember(ch, KEYWORD_START);
  }

  public boolean nonprintable(char ch) {
    return isMember(ch, NONPRINTABLE);
  }

  public boolean propertyStart(char ch) {
    return isMember(ch, PROPERTY_START);
  }

  public boolean ruleStart(char ch) {
    return isMember(ch, PROPERTY_START | VARIABLE_START);
  }

  public boolean selectorEnd(char ch) {
    return isMember(ch, SELECTOR_END);
  }

  public boolean skippable(char ch) {
    return ch == ';' || whitespace(ch);
  }

  public boolean uppercase(char ch) {
    return isMember(ch, UPPERCASE);
  }

  public boolean whitespace(char ch) {
    return (ch >= '\t' && ch <= '\r')
        || (ch == ' ')
        // v8 JavaScript engine's whitespace ranges follow
        || (ch == '\u00a0')
        || (ch == '\u1680')
        || (ch == '\u180e')
        || (ch >= '\u2000' && ch <= '\u200a')
        || (ch >= '\u2028' && ch <= '\u2029')
        || (ch == '\u202f')
        || (ch == '\u205f')
        || (ch == '\u3000')
        || (ch == '\ufeff');
  }

  @Override
  public boolean isMember(char ch, int cls) {
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
        return ENCODE_URI
            | ENCODE_URI_COMPONENT
            ;

      case '#':
        return ENCODE_URI
            | ESCAPE
            ;

      case '$':
        return ENCODE_URI;

      case '%':
        return CALL_START
            | SHORTHAND
            ;

      case '&':
        return ENCODE_URI;

      case '\'':
        return ENCODE_URI
            | ENCODE_URI_COMPONENT
            ;

      case '(':
        return ENCODE_URI
            | ENCODE_URI_COMPONENT
            | ESCAPE
            ;

      case ')':
        return SELECTOR_END
            | ENCODE_URI
            | ENCODE_URI_COMPONENT
            | ESCAPE
            ;

      case '*':
        return PROPERTY_START
            | ENCODE_URI
            | ENCODE_URI_COMPONENT
            ;

      case '+':
        return DIMENSION_START
            | COMBINATOR
            | ENCODE_URI
            ;

      case ',':
        return SELECTOR_END
            | ENCODE_URI
            ;

      case '-':
        return CALL_START
            | DIMENSION_START
            | DIRECTIVE
            | IDENTIFIER
            | KEYWORD_START
            | PROPERTY
            | PROPERTY_START
            | ENCODE_URI
            | ENCODE_URI_COMPONENT
            | SHORTHAND
            | WORD
            ;

      case '.':
        return DIMENSION_START
            | ENCODE_URI
            | ENCODE_URI_COMPONENT
            | PROGID_WORD
            | SHORTHAND
            ;

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
        return DIGIT
            | PROPERTY
            | IDENTIFIER
            | IDENTIFIER_START
            | HEXDIGIT
            | HEXWILD
            | DIMENSION_START
            | PROGID_WORD
            | PROPERTY_START
            | SHORTHAND
            | WORD
            ;

      case ':':
        return ENCODE_URI
            | ESCAPE
            ;

      case ';':
        return SELECTOR_END
            | ENCODE_URI
            | ESCAPE
            ;

      case '=':
        return ENCODE_URI
            | ESCAPE
            ;

      case '>':
        return COMBINATOR;

      case '?':
        return ENCODE_URI
            | HEXWILD
            ;

      case '@':
        return SHORTHAND
            | VARIABLE_START
            | ENCODE_URI;


      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
        return IDENTIFIER
            | IDENTIFIER_START
            | HEXDIGIT
            | HEXWILD
            | UPPERCASE
            | CALL_START
            | KEYWORD_START
            | PROGID_WORD
            | SHORTHAND
            | WORD
            ;

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
        return UPPERCASE
            | IDENTIFIER
            | IDENTIFIER_START
            | CALL_START
            | KEYWORD_START
            | PROGID_WORD
            | SHORTHAND
            | WORD
            ;

      case '_':
        return CALL_START
            | DIRECTIVE
            | IDENTIFIER
            | IDENTIFIER_START
            | KEYWORD_START
            | PROGID_WORD
            | PROPERTY
            | PROPERTY_START
            | ENCODE_URI
            | ENCODE_URI_COMPONENT
            | WORD
            ;

      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
        return DIRECTIVE
            | IDENTIFIER
            | IDENTIFIER_START
            | HEXDIGIT
            | HEXWILD
            | LOWERCASE
            | CALL_START
            | KEYWORD_START
            | PROGID_WORD
            | PROPERTY
            | PROPERTY_START
            | SHORTHAND
            | WORD
            ;

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
        return DIRECTIVE
            | IDENTIFIER
            | IDENTIFIER_START
            | LOWERCASE
            | CALL_START
            | KEYWORD_START
            | PROGID_WORD
            | PROPERTY
            | PROPERTY_START
            | SHORTHAND
            | WORD
            ;

      case '{':
        return SELECTOR_END;

      case '|':
        return COMBINATOR;

      case '}':
        return SELECTOR_END;

      case '~':
        return COMBINATOR
            | ENCODE_URI
            | ENCODE_URI_COMPONENT
            ;

      default:
        break;
    }

    return (ch >= Chars.NO_BREAK_SPACE) ? NONASCII : 0;
  }

}
