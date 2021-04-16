package com.squarespace.less.match;

import static com.squarespace.less.match.Recognizers.FAIL;
import static com.squarespace.less.match.Recognizers.any;
import static com.squarespace.less.match.Recognizers.cardinality;
import static com.squarespace.less.match.Recognizers.charRange;
import static com.squarespace.less.match.Recognizers.characters;
import static com.squarespace.less.match.Recognizers.choice;
import static com.squarespace.less.match.Recognizers.decimal;
import static com.squarespace.less.match.Recognizers.digit;
import static com.squarespace.less.match.Recognizers.digits;
import static com.squarespace.less.match.Recognizers.hexdigit;
import static com.squarespace.less.match.Recognizers.literal;
import static com.squarespace.less.match.Recognizers.lookAhead;
import static com.squarespace.less.match.Recognizers.notAscii;
import static com.squarespace.less.match.Recognizers.notCharRange;
import static com.squarespace.less.match.Recognizers.notCharacters;
import static com.squarespace.less.match.Recognizers.notHexdigit;
import static com.squarespace.less.match.Recognizers.notWhitespace;
import static com.squarespace.less.match.Recognizers.oneOrMore;
import static com.squarespace.less.match.Recognizers.sequence;
import static com.squarespace.less.match.Recognizers.whitespace;
import static com.squarespace.less.match.Recognizers.zeroOrMore;
import static com.squarespace.less.match.Recognizers.zeroOrOne;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.CharClass;

public class RecognizersTest {

  private static final CharClass CLASSIFIER = new CharClass();

  @Test
  public void testAny() {

    // REGEX  xy.z
    Recognizer pattern = sequence(literal("xy"), any(), characters('z'));

    assertEquals(match(pattern, "xy_z"), 4);
    assertEquals(match(pattern, "xy:z"), 4);

    assertEquals(match(pattern, 3, "___xyqz"), 7);
    assertEquals(match(pattern, 3, "___xy~z"), 7);

    assertEquals(match(pattern, "xy"), FAIL);
    assertEquals(match(pattern, "xy_"), FAIL);
  }

  @Test
  public void testCardinality() {

    // REGEX  [.:]{2,5}
    Recognizer pattern = cardinality(characters('.', ':'), 2, 5);

    assertEquals(match(pattern, ".:"), 2);
    assertEquals(match(pattern, ".:."), 3);
    assertEquals(match(pattern, ".:.:"), 4);
    assertEquals(match(pattern, ".:.:."), 5);

    assertEquals(match(pattern, 3, "___.:"), 5);
    assertEquals(match(pattern, 3, "___.:."), 6);
    assertEquals(match(pattern, 3, "___.:.:"), 7);
    assertEquals(match(pattern, 3, "___.:.:."), 8);

    assertEquals(match(pattern, ".:.:.:::"), 5);
    assertEquals(match(pattern, 3, "___.:.:.:::"), 8);

    assertEquals(match(pattern, "._"), FAIL);

    // REGEX  [.:]{,3}
    pattern = cardinality(characters('.', ':'), 0, 3);
    assertEquals(match(pattern, ""), 0);
    assertEquals(match(pattern, "."), 1);
    assertEquals(match(pattern, ".:"), 2);
    assertEquals(match(pattern, ".:."), 3);
    assertEquals(match(pattern, ".:.:"), 3);

    assertEquals(match(pattern, 3, "___"), 3);
    assertEquals(match(pattern, 3, "___."), 4);
    assertEquals(match(pattern, 3, "___.:"), 5);
    assertEquals(match(pattern, 3, "___.:."), 6);
    assertEquals(match(pattern, 3, "___.:.:"), 6);

    // REGEX  [ab]{3,}
    pattern = cardinality(characters('a', 'b'), 3, 0);

    assertEquals(match(pattern, "abb"), 3);
    assertEquals(match(pattern, "abbb"), 4);
    assertEquals(match(pattern, "abbbbbb"), 7);
    assertEquals(match(pattern, 3, "___abbbbbb"), 10);

    assertEquals(match(pattern, "a"), FAIL);
    assertEquals(match(pattern, "ab"), FAIL);
  }

  @Test
  public void testCharacters() {
    Recognizer pattern = oneOrMore(characters('.', 'a', 'b', 'c'));

    assertEquals(match(pattern, "."), 1);
    assertEquals(match(pattern, "abc"), 3);

    pattern = oneOrMore(notCharacters('.', 'a', 'b', 'c'));
    assertEquals(match(pattern, "x"), 1);
    assertEquals(match(pattern, "xabc"), 1);

    assertEquals(match(pattern, 3, "___xyzabc"), 6);
    assertEquals(match(pattern, "."), FAIL);
    assertEquals(match(pattern, "a"), FAIL);
  }

  @Test
  public void testCharacterClass() {

    // REGEX  \d*\.?\d+
    Recognizer pattern = decimal();
    assertEquals(match(pattern, "1"), 1);
    assertEquals(match(pattern, ".1"), 2);
    assertEquals(match(pattern, "1."), 2);
    assertEquals(match(pattern, "123"), 3);
    assertEquals(match(pattern, "123.45"), 6);
    assertEquals(match(pattern, "123.45abc"), 6);
    assertEquals(match(pattern, "123.45."), 6);

    assertEquals(match(pattern, "."), FAIL);
    assertEquals(match(pattern, ".x"), FAIL);

    assertEquals(match(pattern, 3, "___123.45"), 9);

    // REGEX  [^\d]+
//    pattern = oneOrMore(notCharClass(DIGIT, CLASSIFIER));

//    assertEquals(match(pattern, "abc"), 3);
//    assertEquals(match(pattern, "abc123"), 3);
//    assertEquals(match(pattern, "___abc123"), 6);
//
//    assertEquals(match(pattern, "123"), FAIL);
  }

  @Test
  public void testCharacterRange() {

    // REGEX  [a-zA-Z]+
    Recognizer pattern = oneOrMore(choice(charRange('a', 'z'), charRange('A', 'Z')));

    assertEquals(match(pattern, "aBcDe"), 5);
    assertEquals(match(pattern, "AbCdE"), 5);
    assertEquals(match(pattern, "vWxYz"), 5);
    assertEquals(match(pattern, "VwXyZ"), 5);

    assertEquals(match(pattern, "12abcDE"), FAIL);
    assertEquals(match(pattern, "|{}"), FAIL);

    // REGEX  [^a-z]+
    pattern = oneOrMore(notCharRange('a', 'z'));

    assertEquals(match(pattern, "ABCDE"), 5);
    assertEquals(match(pattern, "VWXYZ"), 5);
    assertEquals(match(pattern, "12345"), 5);
    assertEquals(match(pattern, "\tABC"), 4);
    assertEquals(match(pattern, "|{}"), 3);

    assertEquals(match(pattern, "abCde"), FAIL);
  }

  @Test
  public void testChoice() {

    // REGEX  (.?|:*)
    Recognizer pattern = choice(zeroOrOne(characters('.')), zeroOrMore(characters(':')));

    assertEquals(match(pattern, "."), 1);
    assertEquals(match(pattern, ":"), 1);
    assertEquals(match(pattern, ":::"), 3);
    assertEquals(match(pattern, 3, "___:::"), 6);

    assertEquals(match(pattern, ".."), 1);
    assertEquals(match(pattern, ".:"), 1);

    // choice between 2 zero-length patterns can never fail. always matches current position.
    assertEquals(match(pattern, "x"), 0);
  }

  @Test
  public void testDecimal() {

    // REGEX  \d+\.\d+
    Recognizer pattern = decimal();

    assertEquals(match(pattern, "3.14159"), 7);
    assertEquals(match(pattern, "3.14159___"), 7);
  }

  @Test
  public void testDigits() {

    // REGEX  \d+
    Recognizer pattern = oneOrMore(digit());

    assertEquals(match(pattern, "012"), 3);
    assertEquals(match(pattern, "1234"), 4);

    assertEquals(match(pattern, "a1234"), FAIL);

    // REGEX  \d+
    pattern = digits();
    assertEquals(match(pattern, "012"), 3);
    assertEquals(match(pattern, "1234"), 4);

    assertEquals(match(pattern, "a1234"), FAIL);
  }

  @Test
  public void testHexdigit() {

    // REGEX  [A-Fa-f0-9]+
    Recognizer pattern = oneOrMore(hexdigit());

    assertEquals(match(pattern, "deadcode"), 5);
    assertEquals(match(pattern, "deadc0de"), 8);
    assertEquals(match(pattern, 3, "___deadc0de"), 11);

    assertEquals(match(pattern, "ghijk"), FAIL);

    // REGEX  [^A-Fa-f0-9]+
    pattern = oneOrMore(notHexdigit());
    assertEquals(match(pattern, "nonh_x"), 6);

    assertEquals(match(pattern, "deadc0de"), FAIL);
  }

  @Test
  public void testHexcolor() {
    // REGEX #([A-Fa-f0-9]{3}|[A-Fa-f0-9]{6})
    Recognizer pattern = Recognizers.hexcolor();

    assertEquals(match(pattern, "#aaa"), 4);
    assertEquals(match(pattern, "#aaaa"), FAIL);
    assertEquals(match(pattern, "#112233"), 7);
    assertEquals(match(pattern, "#11223344"), 7); // parse valid up to the '4'
  }

  @Test
  public void testLiteral() {
    // REGEX  foobar
    Recognizer pattern = literal("foobar");

    assertEquals(match(pattern, "foobar"), 6);
    assertEquals(match(pattern, "FOOBAR"), FAIL);
  }

  @Test
  public void testLookAhead() {

    // REGEX  (xy)+(?=[.:])
    Recognizer pattern = sequence(oneOrMore(literal("xy")), lookAhead(characters('.', ':')));

    assertEquals(match(pattern, "xy."), 2);
    assertEquals(match(pattern, "xy:"), 2);
    assertEquals(match(pattern, "xyxy."), 4);
    assertEquals(match(pattern, "xyxyxyxyxyxy:"), 12);

    assertEquals(match(pattern, 3, "___xy."), 5);

    assertEquals(match(pattern, "xy"), FAIL);
    assertEquals(match(pattern, "xyxy"), FAIL);
  }

  @Test
  public void testOneOrMore() {

    // REGEX  [.:]+
    Recognizer pattern = oneOrMore(characters('.', ':'));

    assertEquals(match(pattern, "."), 1);
    assertEquals(match(pattern, ".."), 2);

    assertEquals(match(pattern, ".:.123"), 3);
    assertEquals(match(pattern, ".....123"), 5);

    assertEquals(match(pattern, "1"), FAIL);
    assertEquals(match(pattern, "1.."), FAIL);
  }

  @Test
  public void testNonAscii() {

    // REGEX [^\\u0000-\u009f]+
    Recognizer pattern = oneOrMore(notAscii());

    assertEquals(match(pattern, "\u2018\u2019"), 2);
    assertEquals(match(pattern, "\u2018\u2019abc"), 2);
    assertEquals(match(pattern, 3, "___\u2018\u2019abc"), 5);

    assertEquals(match(pattern, "abc"), FAIL);
    assertEquals(match(pattern, "\u0000"), FAIL);
    assertEquals(match(pattern, "\u009f"), FAIL);
  }

  @Test
  public void testSequence() {

    // REGEX  xy[.:]z
    Recognizer pattern = sequence(literal("xy"), characters('.', ':'), literal("z"));

    assertEquals(match(pattern, "xy.z"), 4);
    assertEquals(match(pattern, "xy:z"), 4);

    assertEquals(match(pattern, "xy.zz"), 4);

    assertEquals(match(pattern, "xy"), FAIL);
    assertEquals(match(pattern, "xy.:z"), FAIL);
    assertEquals(match(pattern, "xyz"), FAIL);
    assertEquals(match(pattern, "xy."), FAIL);
  }

  @Test
  public void testWhitespace() {
    // REGEX  \\s*
    Recognizer pattern = zeroOrMore(whitespace());

    assertEquals(match(pattern, "\n \tabc"), 3);

    // REGEX  [^\\s]*
    pattern = zeroOrMore(notWhitespace());

    assertEquals(match(pattern, "a,.\n\t\r "), 3);
  }

//  @Test
//  public void testWord() {
//
//    // REGEX  \\w+
//    Recognizer pattern = oneOrMore(word());
//
//    assertEquals(match(pattern, "foo"), 3);
//    assertEquals(match(pattern, "foobar"), 6);
//    assertEquals(match(pattern, 3, "___foobar"), 9);
//  }

//  @Test
//  public void testWordDash() {
//
//    // REGEX   [\\w-]+
//    Recognizer pattern = oneOrMore(worddash());
//
//    assertEquals(match(pattern, "foo-bar"), 7);
//    assertEquals(match(pattern, "-foo-bar"), 8);
//    assertEquals(match(pattern, 3, "___-foo-bar"), 11);
//    assertEquals(match(pattern, 3, "___foo__bar"), 11);
//
//    assertEquals(match(pattern, ".foo-bar"), FAIL);
//  }

  @Test
  public void testZeroOrOne() {

    // REGEX [.:]*
    Recognizer pattern = zeroOrOne(characters('.', ':'));

    assertEquals(match(pattern, "abc"), 0);
    assertEquals(match(pattern, "."), 1);
    assertEquals(match(pattern, ":"), 1);
    assertEquals(match(pattern, ".."), 1);
  }

  @Test
  public void testZeroOrMore() {

    // REGEX [.:]*
    Recognizer pattern = zeroOrMore(characters('.', ':'));

    assertEquals(match(pattern, ".:."), 3);
    assertEquals(match(pattern, ".:.:.:"), 6);
    assertEquals(match(pattern, 3, "___.:."), 6);
    assertEquals(match(pattern, 3, "___.:.:.:"), 9);

    assertEquals(match(pattern, "...123"), 3);
    assertEquals(match(pattern, 3, "___...123"), 6);

    assertEquals(match(pattern, "123"), 0);
    assertEquals(match(pattern, "1..."), 0);
    assertEquals(match(pattern, 3, "___1..."), 3);

    // REGEX \.*:
    pattern = sequence(zeroOrMore(characters('.')), characters(':'));

    assertEquals(match(pattern, ":"), 1);
    assertEquals(match(pattern, ".:"), 2);
    assertEquals(match(pattern, "...:"), 4);
    assertEquals(match(pattern, ".....:"), 6);
    assertEquals(match(pattern, 3, "___:"), 4);
    assertEquals(match(pattern, 3, "___.....:"), 9);

    assertEquals(match(pattern, ".:::"), 2);
    assertEquals(match(pattern, ".....:::"), 6);
    assertEquals(match(pattern, 3, "___.....:::"), 9);

    assertEquals(match(pattern, "."), FAIL);
    assertEquals(match(pattern, "..."), FAIL);
    assertEquals(match(pattern, 3, "___..."), FAIL);

    // REGEX \.{,3}
    pattern = zeroOrMore(characters('*'), 3);

    assertEquals(match(pattern, ":::"), 0);
    assertEquals(match(pattern, 3, "___:::"), 3);
    assertEquals(match(pattern, "****"), 3);
    assertEquals(match(pattern, 3, "___****"), 6);
  }

  private int match(Recognizer pattern, String str) {
    return match(pattern, 0, str);
  }

  private int match(Recognizer pattern, int pos, String str) {
    return pattern.match(str, pos, str.length());
  }

}