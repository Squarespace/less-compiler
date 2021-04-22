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

import java.nio.file.Path;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.match.Recognizer;
import com.squarespace.less.match.Recognizers;
import com.squarespace.less.model.Node;


/**
 * Extends the TokenStream with modular parser.
 */
public class LessStream extends Stream {

  private final LessContext context;
  private final Path rootPath;
  private final Path fileName;

  private int matchEnd = -1;
  private Mark tokenPosition = new Mark();
  private Mark position = new Mark();
  private String token;

  public LessStream(LessContext ctx, String raw) {
    this(ctx, raw, null, null);
  }

  public LessStream(LessContext ctx, String raw, Path rootPath, Path fileName) {
    super(raw);
    this.context = ctx;
    this.rootPath = rootPath;
    this.fileName = fileName;
  }

  public LessException parseError(LessException exc) {
    return ParseUtils.parseError(exc, fileName, raw, furthest);
  }

  public LessContext context() {
    return context;
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
    return finish(match(Patterns.AND));
  }

  public boolean matchAnonRuleValue() {
    if (!match(Patterns.ANON_RULE_VALUE)) {
      return false;
    }
    matchEnd--;
    set(index, matchEnd);
    consume();
    return true;
  }

  public boolean matchAttributeKey() {
    return finish(match(Patterns.ATTRIBUTE_KEY));
  }

  public boolean matchAttributeOp() {
    return finish(match(Patterns.ATTRIBUTE_OP));
  }

  public boolean matchBoolOperator() {
    return finish(match(Patterns.BOOL_OPERATOR));
  }

  public boolean matchCallName() {
    if (!match(Patterns.CALL_NAME)) {
      return false;
    }
    // Back up to before the parenthesis.
    set(index, matchEnd - 1);
    consume();
    return true;
  }

  public boolean matchDimensionUnit() {
    return finish(match(Patterns.DIMENSION_UNIT));
  }

  public boolean matchDimensionValue() {
    return finish(match(Patterns.DIMENSION_VALUE));
  }

  public boolean matchDirective() {
    return finish(match(Patterns.DIRECTIVE));
  }

  public boolean matchElement0() {
    return finish(match(Patterns.ELEMENT0));
  }

  public boolean matchElement1() {
    return finish(match(Patterns.ELEMENT1));
  }

  public boolean matchElement2() {
    return finish(match(Patterns.ELEMENT2));
  }

  public boolean matchElement3() {
    return finish(match(Patterns.ELEMENT3));
  }

  public boolean matchHexColor() {
    return finish(match(Patterns.HEXCOLOR));
  }

  public boolean matchIdentifier() {
    return finish(match(Patterns.IDENTIFIER));
  }

  public boolean matchImportant() {
    return finish(match(Patterns.IMPORTANT));
  }

  public boolean matchKeyword() {
    return finish(match(Patterns.KEYWORD));
  }

  public boolean matchMixinName() {
    return finish(match(Patterns.MIXIN_NAME));
  }

  public boolean matchNot() {
    return finish(match(Patterns.NOT));
  }

  public boolean matchOpacity() {
    return finish(match(Patterns.OPACITY));
  }

  public boolean matchProperty() {
    return finish(match(Patterns.PROPERTY));
  }

  public boolean matchRatio() {
    return finish(match(Patterns.RATIO));
  }

  public boolean matchUnicodeRange() {
    return finish(match(Patterns.UNICODE_DESCRIPTOR));
  }

  public boolean matchUrlStart() {
    return finish(match(Patterns.URLSTART));
  }

  public boolean matchWhen() {
    return finish(match(Patterns.WHEN));
  }

  public boolean matchWord() {
    return finish(match(Patterns.WORD));
  }

  public boolean peekShorthand() {
    return peek(Patterns.SHORTHAND);
  }

  private boolean peek(Recognizer r) {
    int i = r.match(this.raw, index, length);
    return i != Recognizers.FAIL;
  }

  private boolean match(Recognizer r) {
    int i = r.match(this.raw, index, length);
    boolean matched = i != Recognizers.FAIL;
    if (matched) {
      matchEnd = i;
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
