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

import static com.squarespace.less.parse.Recognizers.FAIL;
import static com.squarespace.less.parse.Recognizers.any;
import static com.squarespace.less.parse.Recognizers.cardinality;
import static com.squarespace.less.parse.Recognizers.charRange;
import static com.squarespace.less.parse.Recognizers.characters;
import static com.squarespace.less.parse.Recognizers.choice;
import static com.squarespace.less.parse.Recognizers.decimal;
import static com.squarespace.less.parse.Recognizers.digit;
import static com.squarespace.less.parse.Recognizers.digits;
import static com.squarespace.less.parse.Recognizers.hexdigit;
import static com.squarespace.less.parse.Recognizers.literal;
import static com.squarespace.less.parse.Recognizers.lookAhead;
import static com.squarespace.less.parse.Recognizers.notAscii;
import static com.squarespace.less.parse.Recognizers.notCharClass;
import static com.squarespace.less.parse.Recognizers.notCharRange;
import static com.squarespace.less.parse.Recognizers.notCharacters;
import static com.squarespace.less.parse.Recognizers.notHexdigit;
import static com.squarespace.less.parse.Recognizers.oneOrMore;
import static com.squarespace.less.parse.Recognizers.sequence;
import static com.squarespace.less.parse.Recognizers.word;
import static com.squarespace.less.parse.Recognizers.worddash;
import static com.squarespace.less.parse.Recognizers.zeroOrMore;
import static com.squarespace.less.parse.Recognizers.zeroOrOne;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.CharClass;
import com.squarespace.less.parse.Recognizers.Recognizer;


public class RecognizerTest {

  @Test
  public void testAny() {

    // REGEX  xy.z
    Recognizer pattern = sequence(literal("xy"), any(), characters('z'));

    assertEquals(4, match(pattern, "xy_z"));
    assertEquals(4, match(pattern, "xy:z"));

    assertEquals(7, match(pattern, 3, "___xyqz"));
    assertEquals(7, match(pattern, 3, "___xy~z"));

    assertEquals(FAIL, match(pattern, "xy"));
    assertEquals(FAIL, match(pattern, "xy_"));
  }

  @Test
  public void testCardinality() {

    // REGEX  [.:]{2,5}
    Recognizer pattern = cardinality(characters('.', ':'), 2, 5);

    assertEquals(2, match(pattern, ".:"));
    assertEquals(3, match(pattern, ".:."));
    assertEquals(4, match(pattern, ".:.:"));
    assertEquals(5, match(pattern, ".:.:."));

    assertEquals(5, match(pattern, 3, "___.:"));
    assertEquals(6, match(pattern, 3, "___.:."));
    assertEquals(7, match(pattern, 3, "___.:.:"));
    assertEquals(8, match(pattern, 3, "___.:.:."));

    assertEquals(5, match(pattern, ".:.:.:::"));
    assertEquals(8, match(pattern, 3, "___.:.:.:::"));

    assertEquals(FAIL, match(pattern, "._"));

    // REGEX  [.:]{,3}
    pattern = cardinality(characters('.', ':'), 0, 3);
    assertEquals(0, match(pattern, ""));
    assertEquals(1, match(pattern, "."));
    assertEquals(2, match(pattern, ".:"));
    assertEquals(3, match(pattern, ".:."));
    assertEquals(3, match(pattern, ".:.:"));

    assertEquals(3, match(pattern, 3, "___"));
    assertEquals(4, match(pattern, 3, "___."));
    assertEquals(5, match(pattern, 3, "___.:"));
    assertEquals(6, match(pattern, 3, "___.:."));
    assertEquals(6, match(pattern, 3, "___.:.:"));

    // REGEX  [ab]{3,}
    pattern = cardinality(characters('a', 'b'), 3, 0);

    assertEquals(3, match(pattern, "abb"));
    assertEquals(4, match(pattern, "abbb"));
    assertEquals(7, match(pattern, "abbbbbb"));
    assertEquals(10, match(pattern, 3, "___abbbbbb"));

    assertEquals(FAIL, match(pattern, "a"));
    assertEquals(FAIL, match(pattern, "ab"));
  }

  @Test
  public void testCharacters() {
    Recognizer pattern = oneOrMore(characters('.', 'a', 'b', 'c'));

    assertEquals(1, match(pattern, "."));
    assertEquals(3, match(pattern, "abc"));

    pattern = oneOrMore(notCharacters('.', 'a', 'b', 'c'));
    assertEquals(1, match(pattern, "x"));
    assertEquals(1, match(pattern, "xabc"));

    assertEquals(6, match(pattern, 3, "___xyzabc"));
    assertEquals(FAIL, match(pattern, "."));
    assertEquals(FAIL, match(pattern, "a"));
  }

  @Test
  public void testCharacterClass() {

    // REGEX  \d*\.?\d+
    Recognizer pattern = decimal();
    assertEquals(1, match(pattern, "1"));
    assertEquals(2, match(pattern, ".1"));
    assertEquals(2, match(pattern, "1."));
    assertEquals(3, match(pattern, "123"));
    assertEquals(6, match(pattern, "123.45"));
    assertEquals(6, match(pattern, "123.45abc"));
    assertEquals(6, match(pattern, "123.45."));

    assertEquals(FAIL, match(pattern, "."));
    assertEquals(FAIL, match(pattern, ".x"));

    assertEquals(9, match(pattern, 3, "___123.45"));

    // REGEX  [^\d]+
    pattern = oneOrMore(notCharClass(CharClass.DIGIT));

    assertEquals(3, match(pattern, "abc"));
    assertEquals(3, match(pattern, "abc123"));
    assertEquals(6, match(pattern, "___abc123"));

    assertEquals(FAIL, match(pattern, "123"));
  }

  @Test
  public void testCharacterRange() {

    // REGEX  [a-zA-Z]+
    Recognizer pattern = oneOrMore(choice(charRange('a', 'z'), charRange('A', 'Z')));

    assertEquals(5, match(pattern, "aBcDe"));
    assertEquals(5, match(pattern, "AbCdE"));
    assertEquals(5, match(pattern, "vWxYz"));
    assertEquals(5, match(pattern, "VwXyZ"));

    assertEquals(FAIL, match(pattern, "12abcDE"));
    assertEquals(FAIL, match(pattern, "|{}"));

    // REGEX  [^a-z]+
    pattern = oneOrMore(notCharRange('a', 'z'));

    assertEquals(5, match(pattern, "ABCDE"));
    assertEquals(5, match(pattern, "VWXYZ"));
    assertEquals(5, match(pattern, "12345"));
    assertEquals(4, match(pattern, "\tABC"));
    assertEquals(3, match(pattern, "|{}"));

    assertEquals(FAIL, match(pattern, "abCde"));
  }

  @Test
  public void testChoice() {

    // REGEX  (.?|:*)
    Recognizer pattern = choice(zeroOrOne(characters('.')), zeroOrMore(characters(':')));

    assertEquals(1, match(pattern, "."));
    assertEquals(1, match(pattern, ":"));
    assertEquals(3, match(pattern, ":::"));
    assertEquals(6, match(pattern, 3, "___:::"));

    assertEquals(1, match(pattern, ".."));
    assertEquals(1, match(pattern, ".:"));

    // choice between 2 zero-length patterns can never fail. always matches current position.
    assertEquals(0, match(pattern, "x"));
  }

  @Test
  public void testDecimal() {

    // REGEX  \d+\.\d+
    Recognizer pattern = decimal();

    assertEquals(7, match(pattern, "3.14159"));
    assertEquals(7, match(pattern, "3.14159___"));
  }

  @Test
  public void testDigits() {

    // REGEX  \d+
    Recognizer pattern = oneOrMore(digit());

    assertEquals(3, match(pattern, "012"));
    assertEquals(4, match(pattern, "1234"));

    assertEquals(FAIL, match(pattern, "a1234"));

    // REGEX  \d+
    pattern = digits();
    assertEquals(3, match(pattern, "012"));
    assertEquals(4, match(pattern, "1234"));

    assertEquals(FAIL, match(pattern, "a1234"));
  }

  @Test
  public void testHexdigit() {

    // REGEX  [A-Fa-f0-9]+
    Recognizer pattern = oneOrMore(hexdigit());

    assertEquals(5, match(pattern, "deadcode"));
    assertEquals(8, match(pattern, "deadc0de"));
    assertEquals(11, match(pattern, 3, "___deadc0de"));

    assertEquals(FAIL, match(pattern, "ghijk"));

    // REGEX  [^A-Fa-f0-9]+
    pattern = oneOrMore(notHexdigit());
    assertEquals(6, match(pattern, "nonh_x"));

    assertEquals(FAIL, match(pattern, "deadc0de"));
  }

  @Test
  public void testLiteral() {
    // REGEX  foobar
    Recognizer pattern = literal("foobar");

    assertEquals(6, match(pattern, "foobar"));
    assertEquals(FAIL, match(pattern, "FOOBAR"));
  }

  @Test
  public void testLookAhead() {

    // REGEX  (xy)+(?=[.:])
    Recognizer pattern = sequence(oneOrMore(literal("xy")), lookAhead(characters('.', ':')));

    assertEquals(2, match(pattern, "xy."));
    assertEquals(2, match(pattern, "xy:"));
    assertEquals(4, match(pattern, "xyxy."));
    assertEquals(12, match(pattern, "xyxyxyxyxyxy:"));

    assertEquals(5, match(pattern, 3, "___xy."));

    assertEquals(FAIL, match(pattern, "xy"));
    assertEquals(FAIL, match(pattern, "xyxy"));
  }

  @Test
  public void testOneOrMore() {

    // REGEX  [.:]+
    Recognizer pattern = oneOrMore(characters('.', ':'));

    assertEquals(1, match(pattern, "."));
    assertEquals(2, match(pattern, ".."));

    assertEquals(3, match(pattern, ".:.123"));
    assertEquals(5, match(pattern, ".....123"));

    assertEquals(FAIL, match(pattern, "1"));
    assertEquals(FAIL, match(pattern, "1.."));
  }

  @Test
  public void testNonAscii() {

    // REGEX [^\\u0000-\u009f]+
    Recognizer pattern = oneOrMore(notAscii());

    assertEquals(2, match(pattern, "\u2018\u2019"));
    assertEquals(2, match(pattern, "\u2018\u2019abc"));
    assertEquals(5, match(pattern, 3, "___\u2018\u2019abc"));

    assertEquals(FAIL, match(pattern, "abc"));
    assertEquals(FAIL, match(pattern, "\u0000"));
    assertEquals(FAIL, match(pattern, "\u009f"));
  }

  @Test
  public void testSequence() {

    // REGEX  xy[.:]z
    Recognizer pattern = sequence(literal("xy"), characters('.', ':'), literal("z"));

    assertEquals(4, match(pattern, "xy.z"));
    assertEquals(4, match(pattern, "xy:z"));

    assertEquals(4, match(pattern, "xy.zz"));

    assertEquals(FAIL, match(pattern, "xy"));
    assertEquals(FAIL, match(pattern, "xy.:z"));
    assertEquals(FAIL, match(pattern, "xyz"));
    assertEquals(FAIL, match(pattern, "xy."));
  }

  @Test
  public void testWord() {

    // REGEX  \\w+
    Recognizer pattern = oneOrMore(word());

    assertEquals(3, match(pattern, "foo"));
    assertEquals(6, match(pattern, "foobar"));
    assertEquals(9, match(pattern, 3, "___foobar"));
  }

  @Test
  public void testWordDash() {

    // REGEX   [\\w-]+
    Recognizer pattern = oneOrMore(worddash());

    assertEquals(7, match(pattern, "foo-bar"));
    assertEquals(8, match(pattern, "-foo-bar"));
    assertEquals(11, match(pattern, 3, "___-foo-bar"));
    assertEquals(11, match(pattern, 3, "___foo__bar"));

    assertEquals(FAIL, match(pattern, ".foo-bar"));
  }

  @Test
  public void testZeroOrOne() {

    // REGEX [.:]*
    Recognizer pattern = zeroOrOne(characters('.', ':'));

    assertEquals(0, match(pattern, "abc"));
    assertEquals(1, match(pattern, "."));
    assertEquals(1, match(pattern, ":"));
    assertEquals(1, match(pattern, ".."));
  }

  @Test
  public void testZeroOrMore() {

    // REGEX [.:]*
    Recognizer pattern = zeroOrMore(characters('.', ':'));

    assertEquals(3, match(pattern, ".:."));
    assertEquals(6, match(pattern, ".:.:.:"));
    assertEquals(6, match(pattern, 3, "___.:."));
    assertEquals(9, match(pattern, 3, "___.:.:.:"));

    assertEquals(3, match(pattern, "...123"));
    assertEquals(6, match(pattern, 3, "___...123"));

    assertEquals(0, match(pattern, "123"));
    assertEquals(0, match(pattern, "1..."));
    assertEquals(3, match(pattern, 3, "___1..."));

    // REGEX \.*:
    pattern = sequence(zeroOrMore(characters('.')), characters(':'));

    assertEquals(1, match(pattern, ":"));
    assertEquals(2, match(pattern, ".:"));
    assertEquals(4, match(pattern, "...:"));
    assertEquals(6, match(pattern, ".....:"));
    assertEquals(4, match(pattern, 3, "___:"));
    assertEquals(9, match(pattern, 3, "___.....:"));

    assertEquals(2, match(pattern, ".:::"));
    assertEquals(6, match(pattern, ".....:::"));
    assertEquals(9, match(pattern, 3, "___.....:::"));

    assertEquals(FAIL, match(pattern, "."));
    assertEquals(FAIL, match(pattern, "..."));
    assertEquals(FAIL, match(pattern, 3, "___..."));

    // REGEX \.{,3}
    pattern = zeroOrMore(characters('*'), 3);

    assertEquals(0, match(pattern, ":::"));
    assertEquals(3, match(pattern, 3, "___:::"));
    assertEquals(3, match(pattern, "****"));
    assertEquals(6, match(pattern, 3, "___****"));
  }

  private int match(Recognizers.Recognizer pattern, String str) {
    return match(pattern, 0, str);
  }

  private int match(Recognizer pattern, int pos, String str) {
    return pattern.match(str, pos, str.length());
  }

}
