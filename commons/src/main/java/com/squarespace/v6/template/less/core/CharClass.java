package com.squarespace.v6.template.less.core;


/**
 * Custom character classifications for CSS/LESS. Builds an internal table which
 * contains flags for each character from 0x00 through 0x80. Used in place of 
 * a switch() or if/else block for each character class.
 */
public class CharClass {

  public static final int DIGIT = 0x01;
  public static final int LOWERCASE = 0x02;
  public static final int UPPERCASE = 0x04;
  public static final int KEYWORD_START = 0x08;
  public static final int DIMENSION_START = 0x10;
  public static final int NONASCII = 0x20;
  public static final int NONPRINTABLE = 0x40;
  public static final int WHITESPACE = 0x80;
  public static final int CALL_START = 0x100;
  public static final int COMBINATOR = 0x200;
  public static final int SELECTOR_END = 0x400;
  public static final int PROPERTY_START = 0x800;
  public static final int VARIABLE_START = 0x1000;
  public static final int SKIPPABLE = 0x2000;
  public static final int ENCODE_URI = 0x4000;
  public static final int ENCODE_URI_COMPONENT = 0x8000;
  public static final int ESCAPE = 0x10000;
  
  // The characters we care about all live below this limit.
  private static final int LIMIT = 0x80;
  
  private static final int[] CLASSES = new int[LIMIT];

  // Build a table for classification
  static {
    for (int i = 0; i < LIMIT; i++) {
      CLASSES[i] = classify((char)i);
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
    return isMember(ch, SKIPPABLE);
  }
  
  public static boolean uppercase(char ch) {
    return isMember(ch, UPPERCASE);
  }
  
  public static boolean whitespace(char ch) {
    return isMember(ch, WHITESPACE)
        // v8 JavaScript engine's whitespace ranges follow
        || (ch == '\u00a1') 
        || (ch == '\u1680') 
        || (ch == '\u180e')
        || (ch >= '\u2000' && ch <= '\u200a')
        || (ch >= '\u2028' && ch <= '\u202a')
        || (ch >= '\u202f' && ch <= '\u202f')
        || (ch == '\u205f')
        || (ch == '\u3000')
        || (ch == '\ufeff');
  }
  
  public static boolean isMember(char ch, int cls) {
    return (ch >= LIMIT) ? false : (CLASSES[ch] & cls) > 0;
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
        
      case '\t':      // 0x09
      case '\n':      // 0x0A
      case '\u000B':
      case '\f':      // 0x0C
      case '\r':      // 0x0D
        return WHITESPACE | SKIPPABLE;
        
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
        
      case ' ':
        return WHITESPACE | SKIPPABLE;
        
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
        return CALL_START | DIMENSION_START | KEYWORD_START | PROPERTY_START | ENCODE_URI | ENCODE_URI_COMPONENT;
        
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
        return DIGIT | DIMENSION_START | PROPERTY_START;
      
      case ':':
        return ENCODE_URI | ESCAPE;
        
      case ';':
        return SELECTOR_END | SKIPPABLE | ENCODE_URI | ESCAPE;

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
        return UPPERCASE | CALL_START | KEYWORD_START;
        
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
        return CALL_START | KEYWORD_START | PROPERTY_START | ENCODE_URI | ENCODE_URI_COMPONENT;
        
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
        return LOWERCASE | CALL_START | KEYWORD_START | PROPERTY_START;
        
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
    }

    return (ch >= Chars.NO_BREAK_SPACE) ? NONASCII : 0;
  }
  
}
