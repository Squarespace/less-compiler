package com.squarespace.less.match;

import com.squarespace.less.core.CharClass;

/**
 * Simple pattern recognizer state machines.
 *
 * Recognizers constructed by this class return the ending position of the
 * match, or FAIL for no match.
 *
 * Faster than equivalent java.util.regex regular expressions due to lower overhead:
 *
 *  - recognizers are optimized for the patterns we care about
 *  - recognizers have fewer variables to update
 *  - no need for a separate java.util.regex.Matcher object
 *  - no region() or reset() calls required when matching against a
 *    different string or position.
 */
public class Recognizers {

  /**
   * Return value when a pattern fails to match.
   */
  public static final int FAIL = -1;

  private static final Recognizer ANY = new Any();
  private static final Recognizer WHITESPACE = new Whitespace(false);
  private static final Recognizer NOT_WHITESPACE = new Whitespace(true);
  private static final CharClass CLASSIFIER = new CharClass();
  private static final Recognizer DIGITS = digits();

  private Recognizers() { }

  public static Recognizer any() {
    return ANY;
  }

  public static Recognizer cardinality(Recognizer pattern, int minimum, int maximum) {
    return new Cardinality(pattern, minimum, maximum);
  }

  public static Recognizer characters(char first, char... characters) {
    return new Characters(false, first, characters);
  }

  public static Recognizer charClass(int bitmask, CharClassifier classifier) {
    return new CharacterClass(bitmask, classifier);
  }

  public static Recognizer charRange(char start, char end) {
    return new CharacterRange(start, end);
  }

  public static Recognizer choice(Recognizer... patterns) {
    return new Recognizers.Choice(patterns);
  }

  public static Recognizer decimal() {
    return new Recognizers.Decimal();
  }

  public static Recognizer digit() {
    return charClass(CharClass.DIGIT, CLASSIFIER);
  }

  public static Recognizer digits() {
    return oneOrMore(digit());
  }

  public static Recognizer dimension() {
    return new Dimension();
  }

  public static Recognizer hexdigit() {
    return charClass(CharClass.HEXDIGIT, CLASSIFIER);
  }

  public static Recognizer hexcolor() {
    return sequence(literal("#"), new LengthChoice(hexdigit(), 3, 6));
  }

  public static Recognizer literal(String str) {
    return new Literal(str);
  }

  public static Recognizer literal(String str, boolean ignoreCase) {
    return new Literal(str, ignoreCase);
  }

  public static Recognizer lookAhead(Recognizer pattern) {
    return new LookAhead(pattern);
  }

  public static Recognizer oneOrMore(Recognizer pattern) {
    return new Plus(pattern);
  }

  public static Recognizer notAscii() {
    return notCharRange('\u0000', '\u009f');
  }

  public static Recognizer notCharacters(char first, char... characters) {
    return new Characters(true, first, characters);
  }

  public static Recognizer notCharClass(int bitmask, CharClassifier classifier) {
    return new CharacterClass(bitmask, true, classifier);
  }

  public static Recognizer notCharRange(char start, char end) {
    return new CharacterRange(true, start, end);
  }

  public static Recognizer notHexdigit() {
    return notCharClass(CharClass.HEXDIGIT, CLASSIFIER);
  }

  public static Recognizer sequence(Recognizer... patterns) {
    return new Sequence(patterns);
  }

  public static Recognizer units() {
    return new Unit();
  }

  public static Recognizer word() {
    return oneOrMore(choice(charClass(CharClass.LOWERCASE | CharClass.UPPERCASE | CharClass.DIGIT, CharClass.CLASSIFIER), characters('_')));
  }

  public static Recognizer whitespace() {
    return WHITESPACE;
  }

  public static Recognizer notWhitespace() {
    return NOT_WHITESPACE;
  }

//  public static Recognizer word() {
//    return charClass(CharClass.LOWERCASE | CharClass.UPPERCASE | CharClass.DIGIT | CharClass.UNDERSCORE, CLASSIFIER);
//  }
//
//  public static Recognizer worddash() {
//    return charClass(LOWERCASE | UPPERCASE | DIGIT | UNDERSCORE | DASH, CLASSIFIER);
//  }

  public static Recognizer zeroOrOne(Recognizer pattern) {
    return new QuestionMark(pattern);
  }

  public static Recognizer zeroOrMore(Recognizer pattern) {
    return new Star(pattern);
  }

  public static Recognizer zeroOrMore(Recognizer pattern, int limit) {
    return new Cardinality(pattern, limit);
  }

  public static class Any implements Recognizer {

    @Override
    public int match(CharSequence seq, int pos, int len) {
      return (pos < len) ? pos + 1 : FAIL;
    }

  }

  /**
   * Matches zero or more of the child matcher.
   */
  static class Cardinality implements Recognizer {

    private final Recognizer pattern;

    private final int start;

    private final int limit;

    Cardinality(Recognizer pattern, int limit) {
      this(pattern, 0, limit);
    }

    Cardinality(Recognizer pattern, int start, int limit) {
      this.pattern = pattern;
      this.start = start;
      this.limit = limit;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int result = pos;
      int count = 0;
      while (true) {
        pos = pattern.match(seq, pos, length);
        if (pos == FAIL) {
          break;
        }
        count++;
        result = pos;
        if (count == limit) {
          break;
        }
      }
      return start > 0 ? (count < start ? FAIL : result) : result;
    }
  }

  /**
   * Match any of a list of characters.
   */
  static class Characters implements Recognizer {

    private final boolean invert;

    private final char first;

    private final char[] chars;

    Characters(boolean invert, char first, char... chars) {
      this.invert = invert;
      this.first = first;
      this.chars = chars;
    }

    @Override
    public int match(CharSequence seq, int pos, int len) {
      if (pos < len) {
        char actual = seq.charAt(pos);
        if (actual == first) {
          return invert ? FAIL : pos + 1;
        }
        for (char expected : chars) {
          if (actual == expected) {
            return invert ? FAIL : pos + 1;
          }
        }
        // Nothing matched, so inverse succeeds.
        return invert ? pos + 1 : FAIL;
      }
      return FAIL;
    }
  }

  /**
   * Matches if the character is a member of the given character class.
   */
  static class CharacterClass implements Recognizer {

    private final int bitmask;

    private final boolean invert;

    private final CharClassifier classifier;

    CharacterClass(int charClass, CharClassifier classifier) {
      this(charClass, false, classifier);
    }

    CharacterClass(int charClass, boolean invert, CharClassifier classifier) {
      this.bitmask = charClass;
      this.invert = invert;
      this.classifier = classifier;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      if (pos < length) {
        boolean result = this.classifier.isMember(seq.charAt(pos), bitmask);
        return invert ? (result ? FAIL : pos + 1) : (result ? pos + 1 : FAIL);
      }
      return FAIL;
    }

  }


  /**
   * Returns next position if the character is within the given range.
   * Setting the invert flag, the match will succeed if the character is not
   * within the given range.
   */
  static class CharacterRange implements Recognizer {

    private final boolean invert;

    private final char start;

    private final char end;

    CharacterRange(char start, char end) {
      this(false, start, end);
    }

    CharacterRange(boolean invert, char start, char end) {
      this.invert = invert;
      this.start = start;
      this.end = end;
    }

    @Override
    public int match(CharSequence seq, int pos, int len) {
      if (pos < len) {
        char ch = seq.charAt(pos);
        boolean result = invert ? (ch < start || ch > end) : (ch >= start && ch <= end);
        return result ? pos + 1 : FAIL;
      }
      return FAIL;
    }

  }

  /**
   * Return result from first matcher that matches.
   */
  static class Choice implements Recognizer {

    private final Recognizer[] patterns;

    Choice(Recognizer[] patterns) {
      this.patterns = patterns;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int save = FAIL;
      for (Recognizer pattern : patterns) {
        // Try choices until we see one that advances past current position.
        int res = pattern.match(seq, pos, length);
        if (res > save) {
          save = Math.max(save, res);
        }
      }
      return save;
    }
  }

  /**
   * Optimized to match decimal with zero or more leading digits and
   * one optional decimal point. Must have at least 1 digit to pass,
   * regardless if decimal is leading or trailing.
   */
  static class Decimal implements Recognizer {

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int save = pos;
      int res = FAIL;
      boolean dot = false;
      while (pos < length) {
        char ch = seq.charAt(pos);
        if (ch == '.') {
          if (dot) {
            break;
          }
          dot = true;

        } else if (!CLASSIFIER.digit(ch)) {
          break;
        }
        pos++;
        res = pos;
      }
      return (dot && (save == pos - 1)) ? FAIL : res;
    }

  }

  /**
   * Special pattern to recognize a decimal number that can start
   * with a bare '.' character.
   *
   * Examples of valid sequences are:
   *
   *   1
   *   3.4
   *   -1.2
   *   +.3
   *
   *  Invalid sequences:
   *
   *    .x
   *    1.x
   *    --2
   *    -+2
   */
  static class Dimension implements Recognizer {

    @Override
    public int match(CharSequence seq, int pos, int len) {
      int save = pos;
      int res = FAIL;
      while (pos < len) {
        char ch = seq.charAt(pos);
        if (save == pos && (ch == '-' || ch == '+')) {
          pos++;
          continue;
        }

        // First '.' we see, match at least one following digit
        if (ch == '.') {
          pos++;
          return DIGITS.match(seq, pos, len);
        }

        pos = DIGITS.match(seq, pos, len);
        if (pos == FAIL) {
          return res;
        }
        res = pos;
      }
      return res;
    }
  }

  /**
   * LengthChoice. Pattern can be one of several unique lengths.
   * Lengths must be ordered from smallest to largest.
   */
  static class LengthChoice implements Recognizer {

    private final int[] choices;
    private final Recognizer cardinality;

    LengthChoice(Recognizer pattern, int... choices) {
      this.choices = choices;
      int min = 0;
      int max = 1;
      if (choices.length > 0) {
        min = choices[0];
        max = choices[choices.length - 1];
      }
      this.cardinality = new Cardinality(pattern, min, max);
    }

    @Override
    public int match(CharSequence seq, int pos, int len) {
      int i = this.cardinality.match(seq, pos, len);
      if (i != FAIL) {
        int sz = i - pos;
        for (int j = 0; j < this.choices.length; j++) {
          if (sz == this.choices[j]) {
            return i;
          }
        }
      }
      return FAIL;
    }
  }

  /**
   * Match an entire literal string.
   */
  static class Literal implements Recognizer {

    private final String literal;
    private final int literalLength;
    private final boolean ignoreCase;

    Literal(String value) {
      this(value, false);
    }

    Literal(String value, boolean ignoreCase) {
      this.literal = value;
      this.literalLength = literal.length();
      this.ignoreCase = ignoreCase;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      if (literalLength <= (length - pos)) {
        if (ignoreCase) {
          for (int i = 0; i < literalLength; i++, pos++) {
            if (literal.charAt(i) != Character.toLowerCase(seq.charAt(pos))) {
              return FAIL;
            }
          }

        } else {
          for (int i = 0; i < literalLength; i++, pos++) {
            if (literal.charAt(i) != seq.charAt(pos)) {
              return FAIL;
            }
          }
        }
        return pos;
      }
      return FAIL;
    }
  }

  /**
   * Returns current position if the child matcher matches, and zero if
   * the matcher fails.
   */
  static class LookAhead implements Recognizer {

    private final Recognizer pattern;

    LookAhead(Recognizer pattern) {
      this.pattern = pattern;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      return (pattern.match(seq, pos, length) != FAIL) ? pos : FAIL;
    }

  }

  /**
   * One or more.
   */
  static class Plus extends Cardinality {
    Plus(Recognizer pattern) {
      super(pattern, 1, 0);
    }
  }

  /**
   * Zero or one.
   */
  static class QuestionMark extends Cardinality {
    QuestionMark(Recognizer pattern) {
      super(pattern, 0, 1);
    }
  }

  /**
   * Match the exact sequence of child matchers, all or nothing.
   */
  static class Sequence implements Recognizer {

    private final Recognizer[] patterns;

    Sequence(Recognizer[] patterns) {
      this.patterns = patterns;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      int result = 0;
      for (Recognizer pattern : patterns) {
        pos = pattern.match(seq, pos, length);
        if (pos == FAIL) {
          return FAIL;
        }
        result = pos;
      }
      return result;
    }

  }

  /**
   * Zero or more.
   */
  static class Star extends Cardinality {
    Star(Recognizer pattern) {
      super(pattern, 0, 0);
    }
  }

  /**
   * Fast matcher for case-insensitive dimension units.
   */
  static class Unit implements Recognizer {

    static final char[][] C_UNITS = new char[][] {
      { 'h' },
      { 'm' },
    };
    static final char[][] D_UNITS = new char[][] {
      { 'p', 'p', 'x' },
      { 'p', 'c', 'm' },
      { 'p', 'i' },
      { 'e', 'g' },
    };
    static final char[][] E_UNITS = new char[][] {
      { 'x' },
      { 'm' },
    };
    static final char[][] F_UNITS = new char[][] {
      { 'r' },
    };
    static final char[][] G_UNITS = new char[][] {
      { 'r', 'a', 'd' },
    };
    static final char[][] H_UNITS = new char[][] {
      { 'z' },
    };
    static final char[][] I_UNITS = new char[][] {
      { 'n' },
    };
    static final char[][] K_UNITS = new char[][] {
      { 'h', 'z' },
    };
    static final char[][] M_UNITS = new char[][] {
      { 's' },
      { 'm' },
    };
    static final char[][] P_UNITS = new char[][] {
      { 't' },
      { 'c' },
      { 'x' },
    };
    static final char[][] R_UNITS = new char[][] {
      { 'e', 'm' },
      { 'a', 'd' },
    };
    static final char[][] S_UNITS = new char[][] {
      {  },
    };
    static final char[][] T_UNITS = new char[][] {
      { 'u', 'r', 'n' },
    };
    static final char[][] V_UNITS = new char[][] {
      { 'm', 'i', 'n' },
      { 'm', 'a', 'x' },
      { 'h' },
      { 'w' },
      { 'm' },
    };

    @Override
    public int match(CharSequence seq, int pos, int len) {
      if (pos < len) {
        char c0 = seq.charAt(pos);
        pos++;
        switch (c0) {
          case '%':
            return pos;
          case 'c':
          case 'C':
            return _match(seq, pos, len, C_UNITS);
          case 'd':
          case 'D':
            return _match(seq, pos, len, D_UNITS);
          case 'e':
          case 'E':
            return _match(seq, pos, len, E_UNITS);
          case 'f':
          case 'F':
            return _match(seq, pos, len, F_UNITS);
          case 'g':
          case 'G':
            return _match(seq, pos, len, G_UNITS);
          case 'h':
          case 'H':
            return _match(seq, pos, len, H_UNITS);
          case 'i':
          case 'I':
            return _match(seq, pos, len, I_UNITS);
          case 'k':
          case 'K':
            return _match(seq, pos, len, K_UNITS);
          case 'm':
          case 'M':
            return _match(seq, pos, len, M_UNITS);
          case 'p':
          case 'P':
            return _match(seq, pos, len, P_UNITS);
          case 'r':
          case 'R':
            return _match(seq, pos, len, R_UNITS);
          case 's':
          case 'S':
            return _match(seq, pos, len, S_UNITS);
          case 't':
          case 'T':
            return _match(seq, pos, len, T_UNITS);
          case 'v':
          case 'V':
            return _match(seq, pos, len, V_UNITS);

        }
      }
      return FAIL;
    }

    private static int _match(CharSequence seq, int pos, int len, char[][] exts) {
      int p = pos;
      for (int i = 0; i < exts.length; i++) {
        char[] ext = exts[i];
        p = pos;
        int j = 0;
        while (j < ext.length && p < len) {
          char ch = Character.toLowerCase(seq.charAt(p));
          if (ch != ext[j]) {
            break;
          }
          j++;
          p++;
        }
        // Matched
        if (j == ext.length) {
          return p;
        }
      }
      return FAIL;
    }
  }

  /**
   * CODE TO UPDATE THE UNITS TABLES ABOVE

    UNITS = "px|%|em|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn|fr|dpi|dpcm|dppx|rem|vw|vh|vmin|vmax|ch|hz|khz|vm"
    from collections import defaultdict
    r = sorted(set((u[0], u[1:]) for u in UNITS.split('|')))
    p = None
    d = defaultdict(set)
    [d[c].add(ext) for c, ext in r if c != '%']
    for c, exts in sorted(d.items()):
        if c == '%':
            continue
        exts = sorted(exts, key=lambda e: -len(e))
        print('static final char[][] %s_UNITS = new char[][] {' % c.upper())
        for ext in exts:
            print('  { ' + ', '.join("'%s'" % e for e in ext) + ' },')
        print('};')

    for c in sorted(d.keys()):
        print("case '%s':\ncase '%s':" % (c, c.upper()))
        print('  return _match(seq, pos, len, %s_UNITS);' % c.upper())

   */

  /**
   * Use a method call to detect whitespace.
   */
  static class Whitespace implements Recognizer {

    private final boolean invert;

    Whitespace(boolean invert) {
      this.invert = invert;
    }

    @Override
    public int match(CharSequence seq, int pos, int length) {
      if (pos < length) {
        if (CLASSIFIER.whitespace(seq.charAt(pos))) {
          return invert ? FAIL : pos + 1;
        } else {
          return invert ? pos + 1 : FAIL;
        }
      }
      return FAIL;
    }

  }

}
