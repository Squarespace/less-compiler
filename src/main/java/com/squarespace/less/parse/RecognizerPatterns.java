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

import static com.squarespace.less.parse.Recognizers.any;
import static com.squarespace.less.parse.Recognizers.cardinality;
import static com.squarespace.less.parse.Recognizers.characters;
import static com.squarespace.less.parse.Recognizers.choice;
import static com.squarespace.less.parse.Recognizers.decimal;
import static com.squarespace.less.parse.Recognizers.digits;
import static com.squarespace.less.parse.Recognizers.hexdigit;
import static com.squarespace.less.parse.Recognizers.notAscii;
import static com.squarespace.less.parse.Recognizers.notCharacters;
import static com.squarespace.less.parse.Recognizers.notHexdigit;
import static com.squarespace.less.parse.Recognizers.oneOrMore;
import static com.squarespace.less.parse.Recognizers.sequence;
import static com.squarespace.less.parse.Recognizers.word;
import static com.squarespace.less.parse.Recognizers.worddash;
import static com.squarespace.less.parse.Recognizers.zeroOrMore;
import static com.squarespace.less.parse.Recognizers.zeroOrOne;

import com.squarespace.less.parse.Recognizers.Recognizer;


/**
 * Higher-level pattern matchers composed from lower-level recognizers.
 */
public class RecognizerPatterns {

  public static final Recognizer ATTRIBUTE_KEY = buildAttributeKey();

  public static final Recognizer ATTRIBUTE_OP = buildAttributeOp();

  public static final Recognizer ELEMENT0 = buildElement0();

  public static final Recognizer ELEMENT1 = buildElement1();

  public static final Recognizer ELEMENT2 = buildElement2();

  public static final Recognizer IDENTIFIER = buildIdentifier();

  private static Recognizer buildAttributeKey() {
    // REGEX  "([\\w-]|\\\\.)+"
    return oneOrMore(choice(worddash(), sequence(characters('\\'), any())));
  }

  private static Recognizer buildAttributeOp() {
    // REGEX  "[|~*$^]?="
    return sequence(zeroOrOne(characters('~', '|', '*', '$', '^')), characters('='));
  }

  private static Recognizer buildElement0() {
    // REGEX  "(?:\\d+\\.\\d+|\\d+)%"
    return sequence(choice(decimal(), digits()), characters('%'));
  }

  private static Recognizer buildElement1() {
    // REGEX  "(?:[.#]?|:*)"
    Recognizer prefix = choice(zeroOrOne(characters('.', '#')), zeroOrMore(characters(':')));

    // REGEX  "(?:[\\w-]|[^\\u0000-\\u009f]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+"
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

  private static Recognizer buildElement2() {
    // REGEX  "\\([^)(@]+\\)"
    return sequence(characters('('), oneOrMore(notCharacters('(', ')', '@')), characters(')'));
  }

  private static Recognizer buildIdentifier() {
    // REGEX  "\\w[\\w-]*"
    return sequence(word(), zeroOrMore(worddash()));
  }

}
