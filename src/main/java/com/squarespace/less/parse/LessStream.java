package com.squarespace.less.parse;

import static com.squarespace.less.core.SyntaxErrorMaker.incompleteParse;

import java.nio.file.Path;
import java.util.regex.Matcher;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;


/**
 * Extends the TokenStream with modular parser.
 */
public class LessStream extends Stream {

  private final Matcher match_AND;

  private final Matcher match_ANON_RULE_VALUE;
  
  private final Matcher match_ATTRIBUTE_KEY;
  
  private final Matcher match_ATTRIBUTE_OP;
  
  private final Matcher match_BOOL_OPERATOR;
  
  private final Matcher match_CALL_NAME;

  private final Matcher match_DIGITS;
  
  private final Matcher match_DIMENSION_UNIT;

  private final Matcher match_DIMENSION_VALUE;

  private final Matcher match_DIRECTIVE;
  
  private final Matcher match_ELEMENT0;
  
  private final Matcher match_ELEMENT1;
  
  private final Matcher match_ELEMENT2;
  
  private final Matcher match_ELEMENT3;

  private final Matcher match_HEXCOLOR;
  
  private final Matcher match_IDENTIFIER;
  
  private final Matcher match_IMPORTANT;
  
  private final Matcher match_KEYWORD;
  
  private final Matcher match_MIXIN_NAME;
  
  private final Matcher match_NOT;
  
  private final Matcher match_OPACITY;
  
  private final Matcher match_PROPERTY;
  
  private final Matcher match_RATIO;
  
  private final Matcher match_SHORTHAND;
  
  private final Matcher match_UNICODE_RANGE;
  
  private final Matcher match_URLSTART;
  
  private final Matcher match_WHEN;
  
  private final Matcher match_WORD;
  
  private int matchEnd = -1;
  
  private Mark tokenPosition = new Mark();
  
  private String token;
  
  private Path rootPath;
  
  private Path fileName;
  
  private Mark position = new Mark();
  
  public LessStream(String raw) {
    this(raw, null, null);
  }
  
  public LessStream(String raw, Path rootPath, Path fileName) {
    super(raw);
    this.rootPath = rootPath;
    this.fileName = fileName;
    this.match_AND = Patterns.AND.matcher(raw);
    this.match_ANON_RULE_VALUE = Patterns.ANON_RULE_VALUE.matcher(raw);
    this.match_ATTRIBUTE_KEY = Patterns.ATTRIBUTE_KEY.matcher(raw);
    this.match_ATTRIBUTE_OP = Patterns.ATTRIBUTE_OP.matcher(raw);
    this.match_BOOL_OPERATOR = Patterns.BOOL_OPERATOR.matcher(raw);
    this.match_CALL_NAME = Patterns.CALL_NAME.matcher(raw);
    this.match_DIGITS = Patterns.DIGITS.matcher(raw);
    this.match_DIMENSION_UNIT = Patterns.DIMENSION_UNIT.matcher(raw);
    this.match_DIMENSION_VALUE = Patterns.DIMENSION_VALUE.matcher(raw);
    this.match_DIRECTIVE = Patterns.DIRECTIVE.matcher(raw);
    this.match_ELEMENT0 = Patterns.ELEMENT0.matcher(raw);
    this.match_ELEMENT1 = Patterns.ELEMENT1.matcher(raw);
    this.match_ELEMENT2 = Patterns.ELEMENT2.matcher(raw);
    this.match_ELEMENT3 = Patterns.ELEMENT3.matcher(raw);
    this.match_HEXCOLOR = Patterns.HEXCOLOR.matcher(raw);
    this.match_IDENTIFIER = Patterns.IDENTIFIER.matcher(raw);
    this.match_IMPORTANT = Patterns.IMPORTANT.matcher(raw);
    this.match_KEYWORD = Patterns.KEYWORD.matcher(raw);
    this.match_MIXIN_NAME = Patterns.MIXIN_NAME.matcher(raw);
    this.match_NOT = Patterns.NOT.matcher(raw);
    this.match_OPACITY = Patterns.OPACITY.matcher(raw);
    this.match_PROPERTY = Patterns.PROPERTY.matcher(raw);
    this.match_RATIO = Patterns.RATIO.matcher(raw);
    this.match_SHORTHAND = Patterns.SHORTHAND.matcher(raw);
    this.match_UNICODE_RANGE = Patterns.UNICODE_DESCRIPTOR.matcher(raw);
    this.match_URLSTART = Patterns.URLSTART.matcher(raw);
    this.match_WHEN = Patterns.WHEN.matcher(raw);
    this.match_WORD = Patterns.WORD.matcher(raw);
  }

  public LessException parseError(LessException exc) {
    return ParseUtils.parseError(exc, fileName, raw, furthest);
  }
  
  public Path rootPath() {
    return rootPath;
  }
  
  public Path fileName() {
    return fileName;
  }
  
  /**
   * Make sure the stream was fully read; otherwise, throw an exception.
   */
  public void checkComplete() throws LessException {
    skipWs();
    if (peek() != Chars.EOF) {
      throw parseError(new LessException(incompleteParse()));
    }
  }
  
  /**
   * Skips whitespace and tries each parselet in order until one returns a non-null result.
   */
  public Node parse(Parselet[] parselets) throws LessException {
    skipWs();
    Node result = null;
    Mark pos = mark();
    for (Parselet parselet : parselets) {
      result = parselet.parse(this);
      if (result != null) {
        result.setLineOffset(pos.lineOffset);
        result.setCharOffset(pos.charOffset);
        mark(position);
        break;
      }
    }
    return result;
  }

  public String token() {
    return token;
  }
  
  public boolean matchAnd() {
    return finish(match(match_AND));
  }
  
  public boolean matchAnonRuleValue() {
    if (!match(match_ANON_RULE_VALUE)) {
      return false;
    }
    matchEnd--;
    set(index, matchEnd);
    consume();
    return true;
  }
  
  public boolean matchAttributeKey() {
    return finish(match(match_ATTRIBUTE_KEY));
  }
  
  public boolean matchAttributeOp() {
    return finish(match(match_ATTRIBUTE_OP));
  }
  
  public boolean matchBoolOperator() {
    return finish(match(match_BOOL_OPERATOR));
  }
  
  public boolean matchCallName() {
    if (!match(match_CALL_NAME)) {
      return false;
    }
    // Back up to before the parenthesis.
    set(index, matchEnd - 1);
    consume();
    return true;
  }
  
  public boolean matchDigits() {
    return finish(match(match_DIGITS));
  }
  
  public boolean matchDimensionUnit() {
    return finish(match(match_DIMENSION_UNIT));
  }

  public boolean matchDimensionValue() {
    return finish(match(match_DIMENSION_VALUE));
  }

  public boolean matchDirective() {
    return finish(match(match_DIRECTIVE));
  }
  
  public boolean matchElement0() {
    return finish(match(match_ELEMENT0));
  }

  public boolean matchElement1() {
    return finish(match(match_ELEMENT1));
  }

  public boolean matchElement2() {
    return finish(match(match_ELEMENT2));
  }

  public boolean matchElement3() {
    return finish(match(match_ELEMENT3));
  }

  public boolean matchHexColor() {
    return finish(match(match_HEXCOLOR));
  }

  public boolean matchIdentifier() {
    return finish(match(match_IDENTIFIER));
  }
  
  public boolean matchImportant() {
    return finish(match(match_IMPORTANT));
  }
  
  public boolean matchKeyword() {
    return finish(match(match_KEYWORD));
  }
  
  public boolean matchMixinName() {
    return finish(match(match_MIXIN_NAME));
  }
  
  public boolean matchNot() {
    return finish(match(match_NOT));
  }
  
  public boolean matchOpacity() {
    return finish(match(match_OPACITY));
  }
  
  public boolean matchProperty() {
    return finish(match(match_PROPERTY));
  }
  
  public boolean matchRatio() {
    return finish(match(match_RATIO));
  }
  
  public boolean matchUnicodeRange() {
    return finish(match(match_UNICODE_RANGE));
  }
  
  public boolean matchUrlStart() {
    return finish(match(match_URLSTART));
  }
  
  public boolean matchWhen() {
    return finish(match(match_WHEN));
  }

  public boolean matchWord() {
    return finish(match(match_WORD));
  }

  public boolean peekShorthand() {
    return peek(match_SHORTHAND);
  }
  
  private boolean peek(Matcher matcher) {
    matcher.region(index, length);
    return matcher.lookingAt();
  }
  
  private boolean match(Matcher matcher) {
    matcher.region(index, length);
    boolean matched = matcher.lookingAt();
    if (matched) {
      matchEnd = matcher.end();
    }
    return matched;
  }

  private boolean finish(boolean result) {
    if (result) {
      set(index, matchEnd);
      consume();
    }
    return result;
  }
  
  private void consume() {
    seek(matchEnd - index);
  }
  
  private void set(int start, int end) {
    tokenPosition.index = start;
    tokenPosition.lineOffset = lineOffset;
    tokenPosition.charOffset = charOffset;
    token = raw.substring(start, end);
  }

}
