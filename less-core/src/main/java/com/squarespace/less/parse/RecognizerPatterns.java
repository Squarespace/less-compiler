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

import static com.squarespace.less.core.CharClass.LOWERCASE;
import static com.squarespace.less.core.CharClass.UPPERCASE;
import static com.squarespace.less.parse.Recognizers.any;
import static com.squarespace.less.parse.Recognizers.cardinality;
import static com.squarespace.less.parse.Recognizers.charClass;
import static com.squarespace.less.parse.Recognizers.characters;
import static com.squarespace.less.parse.Recognizers.choice;
import static com.squarespace.less.parse.Recognizers.decimal;
import static com.squarespace.less.parse.Recognizers.digits;
import static com.squarespace.less.parse.Recognizers.hexdigit;
import static com.squarespace.less.parse.Recognizers.literal;
import static com.squarespace.less.parse.Recognizers.lookAhead;
import static com.squarespace.less.parse.Recognizers.notAscii;
import static com.squarespace.less.parse.Recognizers.notCharacters;
import static com.squarespace.less.parse.Recognizers.notHexdigit;
import static com.squarespace.less.parse.Recognizers.oneOrMore;
import static com.squarespace.less.parse.Recognizers.sequence;
import static com.squarespace.less.parse.Recognizers.whitespace;
import static com.squarespace.less.parse.Recognizers.word;
import static com.squarespace.less.parse.Recognizers.worddash;
import static com.squarespace.less.parse.Recognizers.zeroOrMore;
import static com.squarespace.less.parse.Recognizers.zeroOrOne;

import com.squarespace.less.parse.Recognizers.Recognizer;


/**
 * Higher-level pattern matchers composed from lower-level recognizers.
 */
public class RecognizerPatterns {

  public static final Recognizer AND = literal("and");

  public static final Recognizer ANON_RULE_VALUE = buildAnonRuleValue();

  public static final Recognizer ATTRIBUTE_KEY = buildAttributeKey();

  public static final Recognizer ATTRIBUTE_OP = buildAttributeOp();

  public static final Recognizer CONDITION_OP = buildConditionOp();

  public static final Recognizer CALL_NAME = buildCallName();

  public static final Recognizer DIGITS = digits();

  public static final Recognizer DIMENSION_UNIT = buildDimensionUnit();

  public static final Recognizer DIMENSION_VALUE = buildDimensionValue();

  public static final Recognizer ELEMENT0 = buildElement0();

  public static final Recognizer ELEMENT1 = buildElement1();

  public static final Recognizer ELEMENT2 = buildElement2();

  public static final Recognizer ELEMENT3 = buildElement3();

  public static final Recognizer EXTEND = literal(":extend(");

  public static final Recognizer EXTEND_ALL = buildExtendAll();

  public static final Recognizer HEXCOLOR = buildHexColor();

  public static final Recognizer IDENTIFIER = buildIdentifier();

  public static final Recognizer IMPORTANT = buildImportant();

  public static final Recognizer NOT = literal("not");

  public static final Recognizer URLSTART = buildUrlStart();

  public static final Recognizer WHEN = literal("when");

  public static final Recognizer WORD = oneOrMore(word());


  /**
   * Regular expression:  "(?:[^;@+/'\"*`({}-]*);"
   */
  private static Recognizer buildAnonRuleValue() {
    return sequence(
        zeroOrMore(notCharacters(';', '@', '+', '/', '\'', '"', '*', '`', '(', '{', '}', '-')),
        characters(';'));
  }

  /**
   * Regular expression  "([\\w-]|\\\\.)+"
   */
  private static Recognizer buildAttributeKey() {
    return oneOrMore(
        choice(
            worddash(),
            sequence(characters('\\'), any())));
  }

  /**
   * Regular expression  "[|~*$^]?="
   */
  private static Recognizer buildAttributeOp() {
    return sequence(zeroOrOne(characters('~', '|', '*', '$', '^')), characters('='));
  }

  /**
   * Regular expression  "<>|=[<>]?|[<>]=?|!="
   */
  private static Recognizer buildConditionOp() {
    return choice(
        sequence(characters('='), zeroOrOne(characters('=', '<'))),
        sequence(characters('<', '>'), zeroOrOne(characters('=')))
      );
  }

  /**
   * Regular expression  "([\\w-_]+|%|progid:[\\w\\.]+)\\("
   */
  private static Recognizer buildCallName() {
    return sequence(
        choice(
            oneOrMore(worddash()),
            characters('%'),
            sequence(
                literal("progid:"),
                oneOrMore(choice(word(), characters('.'))))
            ),
        characters('('));
  }

  /**
   * Regular expression  "%|[a-z]+"   case-insensitive
   */
  private static Recognizer buildDimensionUnit() {
    return choice(
        characters('%'),
        oneOrMore(charClass(LOWERCASE | UPPERCASE)));
  }

  /**
   * Regular expression  "[+-]?\\d*\\.?\\d+"
   */
  private static Recognizer buildDimensionValue() {
    return sequence(zeroOrOne(characters('-', '+')), decimal());
  }

  /**
   * Regular expression  "(?:\\d+\\.\\d+|\\d+)%"
   */
  private static Recognizer buildElement0() {
    return sequence(digits(), zeroOrOne(sequence(characters('.'), digits())), characters('%'));
  }

  /**
   * Regular expressions:
   *
   *  prefix  "(?:[.#]?|:*)"
   *
   *  suffix  "(?:[\\w-]|[^\\u0000-\\u009f]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+"
   */
  private static Recognizer buildElement1() {
    Recognizer prefix = choice(zeroOrOne(characters('.', '#')), zeroOrMore(characters(':')));

    Recognizer suffix = oneOrMore(choice(
        worddash(),
        notAscii(),
        sequence(
            characters('\\'),
            choice(
                sequence(cardinality(hexdigit(), 1, 6), zeroOrOne(characters(' '))),
                notHexdigit()
                )
            )
        ));
    return sequence(prefix, suffix);
  }

  /**
   * Regular expression  "\\([^)(@]+\\)"
   */
  private static Recognizer buildElement2() {
    return sequence(characters('('), oneOrMore(notCharacters('(', ')', '@')), characters(')'));
  }

  /**
   * Regular expression  "[\\.#](?=@)"
   */
  private static Recognizer buildElement3() {
    return sequence(characters('.', '#'), lookAhead(characters('@')));
  }

  /**
   * Regular expression   "all\\s*[),]"
   */
  private static Recognizer buildExtendAll() {
    return sequence(literal("all"), zeroOrMore(whitespace()), characters(')', ','));
  }

  /**
   * Regular expression  "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})"
   */
  private static Recognizer buildHexColor() {
    Recognizer threeHex = cardinality(hexdigit(), 3, 3);
    return sequence(characters('#'),
        cardinality(threeHex, 1, 2));
  }

  /**
   * Regular expression  "\\w[\\w-]*"
   */
  private static Recognizer buildIdentifier() {
    return sequence(word(), zeroOrMore(worddash()));
  }

  /**
   * Regular expression "! *important"
   */
  private static Recognizer buildImportant() {
    return sequence(characters('!'), zeroOrMore(characters(' ')), literal("important"));
  }

  /**
   * Regular expression  "url\\s*\\("
   */
  private static Recognizer buildUrlStart() {
    return sequence(literal("url"), zeroOrMore(whitespace()), characters('('));
  }

}
