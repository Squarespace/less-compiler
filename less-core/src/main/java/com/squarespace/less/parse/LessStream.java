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

import static com.squarespace.less.core.SyntaxErrorMaker.incompleteParse;
import static com.squarespace.less.parse.RecognizerPatterns.DIGITS;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.core.Constants;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;
import com.squarespace.compiler.match.Recognizers.Recognizer;


/**
 * Extends the TokenStream with modular parser.
 */
public class LessStream extends Stream {

  private final Matcher matcherAttributeKey;

  private final Matcher matcherAttributeOp;

  private final Matcher matcherDirective;

  private final Matcher matcherKeyword;

  private final Matcher matcherMixinName;

  private final Matcher matcherOpacity;

  private final Matcher matcherProperty;

  private final Matcher matcherRatio;

  private final Matcher matcherShorthand;

  private final Matcher matcherUnicodeRange;

  private final LessParser parser;

  private final Path path;

  private final Path rootPath;

  private final Path fileName;

  /**
   * Maintains a list of deferred evaluations during parse.
   */
  private final List<ExecEnv> deferred = new ArrayList<>();

  /**
   * Holds a stack of parse frames, which represent the nested blocks entered
   * during the parse.  This allows us to capture a closure for each import
   * statement encountered that requires interpolation.  We evaluate the
   * import path against that closure immediately after the parse completes.
   */
  private final ExecEnv parseEnv;

  private int matchEnd = -1;

  private Mark tokenPosition = new Mark();

  private String token;

  private boolean inParens;

  // Special mode for 'font' rules.
  private boolean requireStrictMath;

  public LessStream(LessParser parser, String raw) {
    this(parser, raw, null);
  }

  public LessStream(LessParser parser, String raw, Path path) {
    this(parser, raw, null, null);
  }

  public LessStream(LessParser parser, String raw, Path path, ExecEnv env) {
    super(raw);
    this.parser = parser;
    this.path = (path == null) ? Constants.DEFAULT_PATH : path;

    Path parent = this.path.getParent();
    this.rootPath = (parent == null) ? Paths.get(".") : parent;
    this.fileName = this.path.getFileName();
    this.parseEnv = (env == null) ? new ExecEnv(parser.context()) : env;

    this.matcherAttributeKey = Patterns.ATTRIBUTE_KEY.matcher(raw);
    this.matcherAttributeOp = Patterns.ATTRIBUTE_OP.matcher(raw);
    this.matcherDirective = Patterns.DIRECTIVE.matcher(raw);
    this.matcherKeyword = Patterns.KEYWORD.matcher(raw);
    this.matcherMixinName = Patterns.MIXIN_NAME.matcher(raw);
    this.matcherOpacity = Patterns.OPACITY.matcher(raw);
    this.matcherProperty = Patterns.PROPERTY.matcher(raw);
    this.matcherRatio = Patterns.RATIO.matcher(raw);
    this.matcherShorthand = Patterns.SHORTHAND.matcher(raw);
    this.matcherUnicodeRange = Patterns.UNICODE_DESCRIPTOR.matcher(raw);
  }

  public LessException parseError(LessException exc) {
    return ParseUtils.parseError(exc, fileName, raw, furthest);
  }

  public LessParser parser() {
    return parser;
  }

  public LessContext context() {
    return parser.context();
  }

  public List<ExecEnv> deferreds() {
    return deferred;
  }

  public ExecEnv execEnv() {
    return parseEnv;
  }

  public Path path() {
    return path;
  }

  public Path rootPath() {
    return rootPath;
  }

  public Path fileName() {
    return fileName;
  }

  public boolean inParens() {
    return inParens;
  }

  public void setInParens(boolean flag) {
    this.inParens = flag;
  }

  public boolean requireStrictMath() {
    return requireStrictMath;
  }

  public void setRequireStrictMath(boolean flag) {
    this.requireStrictMath = flag;
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
        break;
      }
    }
    return result;
  }

  public String token() {
    return token;
  }

  /**
   * Defer the current block for evaluation after the parse completes.
   * To do this we capture the current stack as a closure.
   */
  public void defer() {
    if (parseEnv != null) {
      Block block = parseEnv.frames().last();
      if (!block.deferred()) {
        block.markDeferred();
        deferred.add(parseEnv.copy());
      }
    }
  }

  public boolean matchAnd() {
    return finish(match(RecognizerPatterns.AND));
  }

  public boolean matchAnonRuleValue() {
    if (!match(RecognizerPatterns.ANON_RULE_VALUE)) {
      return false;
    }
    matchEnd--;
    set(index, matchEnd);
    consume();
    return true;
  }

  public boolean matchAttributeKey() {
    return finish(match(matcherAttributeKey));
  }

  public boolean matchAttributeOp() {
    return finish(match(matcherAttributeOp));
  }

  public boolean matchConditionOp() {
    return finish(match(RecognizerPatterns.CONDITION_OP));
  }

  public boolean matchCallName() {
    if (!match(RecognizerPatterns.CALL_NAME)) {
      return false;
    }
    // Back up to before the parenthesis.
    set(index, matchEnd - 1);
    consume();
    return true;
  }

  public boolean matchDigits() {
    return finish(match(DIGITS));
  }

  public boolean matchDimensionUnit() {
    return finish(match(RecognizerPatterns.DIMENSION_UNIT));
  }

  public boolean matchDimensionValue() {
    return finish(match(RecognizerPatterns.DIMENSION_VALUE));
  }

  public boolean matchDirective() {
    return finish(match(matcherDirective));
  }

  public boolean matchElement0() {
    return finish(match(RecognizerPatterns.ELEMENT0));
  }

  public boolean matchElement1() {
    return finish(match(RecognizerPatterns.ELEMENT1));
  }

  public boolean matchElement2() {
    return finish(match(RecognizerPatterns.ELEMENT2));
  }

  public boolean matchElement3() {
    return finish(match(RecognizerPatterns.ELEMENT3));
  }

  public boolean matchExtend() {
    return finish(match(RecognizerPatterns.EXTEND));
  }

  public boolean matchExtendAll() {
    if (!match(RecognizerPatterns.EXTEND_ALL)) {
      return false;
    }
    // Back up to before the comma or parenthesis.
    matchEnd--;
    set(index, matchEnd);
    consume();
    return true;
  }

  public boolean matchHexColor() {
    return finish(match(RecognizerPatterns.HEXCOLOR));
  }

  public boolean matchIdentifier() {
    return finish(match(RecognizerPatterns.IDENTIFIER));
  }

  public boolean matchImportant() {
    return finish(match(RecognizerPatterns.IMPORTANT));
  }

  public boolean matchKeyword() {
    return finish(match(matcherKeyword));
  }

  public boolean matchMixinName() {
    return finish(match(matcherMixinName));
  }

  public boolean matchNot() {
    return finish(match(RecognizerPatterns.NOT));
  }

  public boolean matchOpacity() {
    return finish(match(matcherOpacity));
  }

  public boolean matchProperty() {
    return finish(match(matcherProperty));
  }

  public boolean matchRatio() {
    return finish(match(matcherRatio));
  }

  public boolean matchUnicodeRange() {
    return finish(match(matcherUnicodeRange));
  }

  public boolean matchUrlStart() {
    return finish(match(RecognizerPatterns.URLSTART));
  }

  public boolean matchWhen() {
    return finish(match(RecognizerPatterns.WHEN));
  }

  public boolean matchWord() {
    return finish(match(RecognizerPatterns.WORD));
  }

  public boolean peekShorthand() {
    return peek(matcherShorthand);
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

  private boolean match(Recognizer recognizer) {
    int pos = recognizer.match(raw, index, length);
    if (pos > index) {
      matchEnd = pos;
      return true;
    }
    return false;
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
