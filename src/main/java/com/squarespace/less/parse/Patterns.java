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
import static com.squarespace.less.match.Recognizers.digits;
import static com.squarespace.less.match.Recognizers.literal;
import static com.squarespace.less.match.Recognizers.sequence;
import static com.squarespace.less.match.Recognizers.whitespace;
import static com.squarespace.less.match.Recognizers.zeroOrMore;

import java.util.regex.Pattern;

import com.squarespace.less.core.CharPattern;
import com.squarespace.less.core.Chars;
import com.squarespace.less.match.Recognizer;
import com.squarespace.less.match.Recognizers;
import com.squarespace.less.model.Unit;


public class Patterns {

  // Common

  private static final String _HEXWILD = "[A-Fa-f0-9?]";

  // Regular expressions.

  // REGEX: "alpha\\s*\\("
  public static final Recognizer ALPHA_START = sequence(literal("alpha"), zeroOrMore(whitespace()), literal("("));

  public static final Recognizer AND = literal("and");

  public static final Pattern ANON_RULE_VALUE = pattern("(?:[^;@+/'\"*`({}-]*);");

  public static final Pattern ATTRIBUTE_KEY = pattern("([\\w-]|\\\\.)+");

  public static final Pattern ATTRIBUTE_OP = pattern("[|~*$^]?=");

  public static final Pattern BOOL_OPERATOR = pattern("<>|=[<>]*|[<>]=*|!=");

  public static final Pattern CALL_NAME = pattern("([\\w-_]+|%|progid:[\\w\\.]+)\\(");

  public static final Recognizer DIGITS = Recognizers.digits();

  public static final Pattern DIMENSION_UNIT = pattern(Unit.REGEX, true);

  public static final Pattern DIMENSION_VALUE = pattern("[+-]?\\d*\\.?\\d+");

  public static final Pattern DIRECTIVE = pattern("@[a-z-]+");

  public static final Pattern ELEMENT0 = pattern("(?:\\d+\\.\\d+|\\d+)%");

  public static final Pattern ELEMENT1 = pattern("(?:[.#]?|:*)(?:[\\w-]|[^\\u0000-\\u009f]|"
        + "\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+");

  public static final Pattern ELEMENT2 = pattern("\\([^)(@]+\\)");

  public static final Pattern ELEMENT3 = pattern("[\\.#](?=@)");

  public static final Recognizer HEXCOLOR = Recognizers.hexcolor();

  // TODO: less allows identifiers starting with a digit, perhaps we should restrict it to [a-zA-Z][\\w-]+
  public static final Pattern IDENTIFIER = pattern("[\\w][\\w-]*", true);

  // REGEX: "! *important"
  public static final Recognizer IMPORTANT = sequence(literal("!"), zeroOrMore(literal(" ")), literal("important"));

  public static final Pattern KEYWORD = pattern("[_A-Za-z-][\\w-]*");

  public static final Recognizer MEDIA = literal("@media");

  public static final Pattern MIXIN_NAME = pattern("[#.](?:[\\w-]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+");

  public static final Recognizer NOT = literal("not");

  public static final Pattern OPACITY = pattern("opacity=", true);

  public static final Pattern OPERATOR = pattern("\\+|\\-|\\*|\\/");

  public static final Pattern PROPERTY = pattern("\\*?-?[_a-z0-9-]+", false);

  public static final Recognizer RATIO = sequence(digits(), literal("/"), digits());

  public static final Pattern SHORTHAND = pattern("[@\\w.%-]+" + "\\/" + "[@\\w.-]+");

  public static final Pattern UNICODE_DESCRIPTOR = pattern("U\\+" + _HEXWILD + "+(\\-" + _HEXWILD + "+)?");

  // REGEX: "url\\s*\\("
  public static final Recognizer URLSTART = sequence(literal("url", true), zeroOrMore(whitespace()), literal("("));

  public static final Pattern _URLEND_BARE = pattern("[^\\s)]+");

  public static final Recognizer WHEN = literal("when");

  public static final Pattern WORD = pattern("\\w+");

  // Character-level patterns.

  public static final CharPattern CDC_CLOSE = new CharPattern(MINUS_SIGN, MINUS_SIGN, GREATER_THAN_SIGN);

  public static final CharPattern COLON = new CharPattern(Chars.COLON);

  public static final CharPattern LINE_COMMENT_END = new CharPattern(LINE_FEED);

  public static final CharPattern LEFT_PAREN = new CharPattern(LEFT_PARENTHESIS);

  public static final CharPattern BLOCK_COMMENT_END = new CharPattern(ASTERISK, SLASH);

  public static final CharPattern RIGHT_PAREN  = new CharPattern(RIGHT_PARENTHESIS);

  public static final CharPattern VARIADIC = new CharPattern(PERIOD, PERIOD, PERIOD);

  private static Pattern pattern(String regex) {
    return pattern(regex, false);
  }

  private static Pattern pattern(String regex, boolean caseInsensitive) {
    int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
    return Pattern.compile(regex, flags);
  }

}
