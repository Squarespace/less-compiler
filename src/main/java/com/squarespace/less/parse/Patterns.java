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

package com.squarespace.less.parse;

import static com.squarespace.less.core.Chars.ASTERISK;
import static com.squarespace.less.core.Chars.GREATER_THAN_SIGN;
import static com.squarespace.less.core.Chars.LEFT_PARENTHESIS;
import static com.squarespace.less.core.Chars.LINE_FEED;
import static com.squarespace.less.core.Chars.MINUS_SIGN;
import static com.squarespace.less.core.Chars.PERIOD;
import static com.squarespace.less.core.Chars.RIGHT_PARENTHESIS;
import static com.squarespace.less.core.Chars.SLASH;
import static com.squarespace.less.match.Recognizers.anon;
import static com.squarespace.less.match.Recognizers.digits;
import static com.squarespace.less.match.Recognizers.literal;
import static com.squarespace.less.match.Recognizers.sequence;
import static com.squarespace.less.match.Recognizers.whitespace;
import static com.squarespace.less.match.Recognizers.zeroOrMore;

import com.squarespace.less.core.CharPattern;
import com.squarespace.less.core.Chars;
import com.squarespace.less.match.Recognizer;
import com.squarespace.less.match.Recognizers;


public class Patterns {

  // Common


  // Regular expressions.

  // REGEX: "alpha\\s*\\("
  // TODO: hand-code
//  public static final Recognizer ALPHA_START = sequence(literal("alpha"), zeroOrMore(whitespace()), literal("("));

//  public static final Recognizer AND = literal("and");

  // REGEX: "(?:[^;@+/'\"*`({}-]*);"
  public static final Recognizer ANON_RULE_VALUE = anon();

  // REGEX: "([\\w-]|\\\\.)+"
  public static final Recognizer ATTRIBUTE_KEY = Recognizers.attributeKey();

  // REGEX: "[|~*$^]?="
  public static final Recognizer ATTRIBUTE_OP = Recognizers.attributeOperator();

  // REGEX: "<>|=[<>]*|[<>]=*|!="
  public static final Recognizer BOOL_OPERATOR = Recognizers.boolOperator();

  // REGEX: "([\\w-_]+|%|progid:[\\w\\.]+)\\("
  public static final Recognizer CALL_NAME = Recognizers.callName();

  public static final Recognizer DIMENSION_UNIT = Recognizers.units();

  public static final Recognizer DIMENSION_VALUE = Recognizers.dimension();

  // REGEX: "@[a-z-]+"
  public static final Recognizer DIRECTIVE = Recognizers.directive();

  public static final Recognizer ELEMENT0 = Recognizers.element0();

  // REGEX: "(?:[.#]?|:*)(?:[\\w-]|[^\\u0000-\\u009f]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+"
  public static final Recognizer ELEMENT1 = Recognizers.element1();

  // REGEX: "\\([^)(@]+\\)"
  public static final Recognizer ELEMENT2 = Recognizers.element2();

  // REGEX: "[\\.#](?=@)"
  public static final Recognizer ELEMENT3 = Recognizers.element3();

  public static final Recognizer ESCAPE = Recognizers.escape();

  public static final Recognizer HEXCOLOR = Recognizers.hexcolor();

  // TODO: less allows identifiers starting with a digit, perhaps we should restrict it to [a-zA-Z][\\w-]+
  // REGEX: "[\\w][\\w-]*"  (ignore case)
  public static final Recognizer IDENTIFIER = Recognizers.identifier();

  // REGEX: "! *important"
  public static final Recognizer IMPORTANT = Recognizers.important();

  // REGEX: "[_A-Za-z-][\\w-]*"
  public static final Recognizer KEYWORD = Recognizers.keyword();

  // "[#.](?:[\\w-]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+"
  public static final Recognizer MIXIN_NAME = Recognizers.mixinName();

  public static final Recognizer NOT = literal("not");

//  public static final Recognizer OPACITY = literal("opacity=", true);

//  public static final Pattern OPERATOR = pattern("\\+|\\-|\\*|\\/");

  // REGEX: "\\*?-?[_a-z0-9-]+"
  public static final Recognizer PROPERTY = Recognizers.property();

  public static final Recognizer RATIO = sequence(digits(), literal("/"), digits());

  // REGEX: "[@\\w.%-]+" + "\\/" + "[@\\w.-]+"
  public static final Recognizer SHORTHAND = Recognizers.shorthand();

  // _HEXWILD = "[A-Fa-f0-9?]";
  // REGEX: "U\\+" + _HEXWILD + "+(\\-" + _HEXWILD + "+)?"
  public static final Recognizer UNICODE_DESCRIPTOR = Recognizers.unicode();

  // REGEX: "url\\s*\\("
  public static final Recognizer URLSTART = sequence(literal("url", true), zeroOrMore(whitespace()), literal("("));

//  public static final Pattern _URLEND_BARE = pattern("[^\\s)]+");

  public static final Recognizer WHEN = literal("when");

  public static final Recognizer WORD = Recognizers.word();

  // Character-level patterns.

  public static final CharPattern CDC_CLOSE = new CharPattern(MINUS_SIGN, MINUS_SIGN, GREATER_THAN_SIGN);

  public static final CharPattern COLON = new CharPattern(Chars.COLON);

  public static final CharPattern LINE_COMMENT_END = new CharPattern(LINE_FEED);

  public static final CharPattern LEFT_PAREN = new CharPattern(LEFT_PARENTHESIS);

  public static final CharPattern BLOCK_COMMENT_END = new CharPattern(ASTERISK, SLASH);

  public static final CharPattern RIGHT_PAREN  = new CharPattern(RIGHT_PARENTHESIS);

  public static final CharPattern VARIADIC = new CharPattern(PERIOD, PERIOD, PERIOD);


}
