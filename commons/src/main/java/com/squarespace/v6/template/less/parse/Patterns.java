package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.core.Chars.ASTERISK;
import static com.squarespace.v6.template.less.core.Chars.GREATER_THAN_SIGN;
import static com.squarespace.v6.template.less.core.Chars.LEFT_PARENTHESIS;
import static com.squarespace.v6.template.less.core.Chars.LINE_FEED;
import static com.squarespace.v6.template.less.core.Chars.MINUS_SIGN;
import static com.squarespace.v6.template.less.core.Chars.PERIOD;
import static com.squarespace.v6.template.less.core.Chars.RIGHT_PARENTHESIS;
import static com.squarespace.v6.template.less.core.Chars.SLASH;

import java.util.regex.Pattern;

import com.squarespace.v6.template.less.core.CharPattern;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Unit;


public class Patterns {

  // Common
  
  private static final String _HEXCHAR = "[A-Fa-f0-9]";

  private static final String _HEXWILD = "[A-Fa-f0-9?]";
  
  // Regular expressions.
  
  public static final Pattern ALPHA_START = pattern("alpha\\s*\\(");
  
  public static final Pattern AND = pattern("and");
  
  public static final Pattern ANON_RULE_VALUE = pattern("(?:[^;@+/'\"*`({}-]*);");
  
  public static final Pattern ATTRIBUTE_KEY = pattern("([\\w-]|\\\\.)+");
  
  public static final Pattern ATTRIBUTE_OP = pattern("[|~*$^]?=");
  
  public static final Pattern BOOL_OPERATOR = pattern("<>|=[<>]*|[<>]=*|!=");
  
  public static final Pattern CALL_NAME = pattern("([\\w-_]+|%|progid:[\\w\\.]+)\\(");

  public static final Pattern DIGITS = pattern("\\d+");
  
  public static final Pattern DIMENSION_UNIT = pattern(Unit.REGEX, true);

  public static final Pattern DIMENSION_VALUE = pattern("[+-]?\\d*\\.?\\d+");

  public static final Pattern DIRECTIVE = pattern("@[a-z-]+");
  
  public static final Pattern ELEMENT0 = pattern("(?:\\d+\\.\\d+|\\d+)%");

  public static final Pattern ELEMENT1 = pattern("(?:[.#]?|:*)(?:[\\w-]|[^\\u0000-\\u009f]|"
        + "\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+");
  
  public static final Pattern ELEMENT2 = pattern("\\([^)(@]+\\)");
  
  public static final Pattern ELEMENT3 = pattern("[\\.#](?=@)");
  
  public static final Pattern HEXCOLOR = pattern("#(" + _HEXCHAR + "{6}|" + _HEXCHAR + "{3})");
  
  // TODO: less allows identifiers starting with a digit, perhaps we should restrict it to [a-zA-Z][\\w-]+
  public static final Pattern IDENTIFIER = pattern("[\\w][\\w-]*", true);
  
  public static final Pattern IMPORTANT = pattern("! *important");
  
  public static final Pattern KEYWORD = pattern("[_A-Za-z-][\\w-]*");
  
  public static final Pattern MEDIA = pattern("@media");
  
  public static final Pattern MIXIN_NAME = pattern("[#.](?:[\\w-]|\\\\(?:[A-Fa-f0-9]{1,6} ?|[^A-Fa-f0-9]))+");
  
  public static final Pattern NOT = pattern("not");
  
  public static final Pattern OPACITY = pattern("opacity=", true);
  
  public static final Pattern OPERATOR = pattern("\\+|\\-|\\*|\\/");
  
  public static final Pattern PROPERTY = pattern("\\*?-?[_a-z0-9-]+", false);
  
  public static final Pattern RATIO = pattern("\\d+\\/\\d+");
  
  public static final Pattern SHORTHAND = pattern("[@\\w.%-]+" + "\\/" + "[@\\w.-]+");
  
  public static final Pattern UNICODE_DESCRIPTOR = pattern("U\\+" + _HEXWILD + "+(\\-" + _HEXWILD + "+)?");
  
  public static final Pattern URLSTART = pattern("url\\s*\\(");
  
  public static final Pattern _URLEND_BARE = pattern("[^\\s)]+");
  
  public static final Pattern URL_BARE = pattern("(?:(?:\\\\[\\(\\)'\"])|[^\\(\\)'\"])+");
  
  public static final Pattern WHEN = pattern("when");
  
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
