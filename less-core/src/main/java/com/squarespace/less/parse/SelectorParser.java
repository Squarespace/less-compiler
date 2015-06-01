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

import static com.squarespace.less.core.Chars.AMPERSAND;
import static com.squarespace.less.core.Chars.APOSTROPHE;
import static com.squarespace.less.core.Chars.ASTERISK;
import static com.squarespace.less.core.Chars.EOF;
import static com.squarespace.less.core.Chars.LINE_FEED;
import static com.squarespace.less.core.Chars.QUOTATION_MARK;
import static com.squarespace.less.core.Chars.RIGHT_SQUARE_BRACKET;
import static com.squarespace.less.core.Chars.TILDE;
import static com.squarespace.less.model.CombinatorType.DESC;
import static com.squarespace.less.parse.RecognizerPatterns.ATTRIBUTE_KEY;
import static com.squarespace.less.parse.RecognizerPatterns.ATTRIBUTE_OP;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT0;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT1;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT2;
import static com.squarespace.less.parse.RecognizerPatterns.IDENTIFIER;

import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessContext;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.CombinatorType;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.SelectorPart;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.WildcardElement;


/**
 * Used to render a Selector and parse it to produce its canonical representation.
 * This is essential for accurate comparisons between selectors.
 *
 * This parses a subset of the syntax covered by {@link ElementParselet}.
 */
public class SelectorParser extends LightweightStream {

  private final LessContext context;

  public SelectorParser(LessContext context) {
    this.context = context;
  }

  /**
   * Parse the raw string into a {@link Selector}, or return null if we failed to consume
   * the entire input.
   */
  public Selector parse(String str) {
    init(str);
    Selector selector = null;

    while (true) {
      // Parse the combinator but hold it to see if we get an element.
      Combinator combinator = (Combinator) parseCombinator(selector == null);

      SelectorPart part = parseSelectorPart();
      if (part == null) {
        break;
      }

      selector = initSelector(context, selector);
      if (combinator != null) {
        selector.add(combinator);
      }
      selector.add(part);
    }
    return (index != length) ? null : selector;
  }

  /**
   * Parse one combinator, element sequence. The combinator may be null.
   */
  private SelectorPart parseSelectorPart() {
    skipWs();
    char ch = peek();

    // The parsing logic below runs a series of tests of the current
    // stream position. When one fails to match it falls back to
    // the next pattern. Note that no variables are parsed, since
    // we do not expect to see them post-evaluation.
    //
    // In order of preference, the patterns attempted are:
    //
    //  element0   - decimal percent
    //  element1   - main class/id pattern
    //  asterisk   - universal selector
    //  ampersand  - wild card selector
    //  attribute  - rich attribute pattern
    //  element2   - parenthesis-wrapped text element

    if (match(ELEMENT0)) {
      return new TextElement(token);

    } else if (match(ELEMENT1)) {
      return new TextElement(token);

    } else if (ch == ASTERISK) {
      seek1();
      return new TextElement(Character.toString(ch));

    } else if (ch == AMPERSAND) {
      seek1();
      return new WildcardElement();
    }

    SelectorPart part = parseAttribute();
    if (part != null) {
      return part;
    }

    if (match(ELEMENT2)) {
      return new TextElement(token);
    }

    // Note: we completely ignore curly variables here as this parser is only
    // used for canonicalizing selectors after they've been evaluated.
    return null;
  }

  /**
   * Parse zero or one combinator characters, skipping over any extraneous whitespace.
   */
  private Combinator parseCombinator(boolean selectorStart) {
    char prev = peek(-1);
    int skipped = skipWs();
    char ch = peek();

    if (CharClass.combinator(ch)) {
      seek1();
      return new Combinator(CombinatorType.fromChar(ch));

    } else if (!selectorStart && (skipped > 0 || CharClass.whitespace(prev))) {
      return new Combinator(DESC);
    }

    return null;
  }

  /**
   * Parse attribute element syntax.
   */
  private SelectorPart parseAttribute() {
    if (!seekIf(Chars.LEFT_SQUARE_BRACKET)) {
      return null;
    }

    int saveIndex = index;
    Node key = null;
    if (match(ATTRIBUTE_KEY)) {
      key = new Anonymous(token);
    } else {
      key = parseQuoted();
    }
    if (key == null) {
      index = saveIndex;
      return null;
    }

    AttributeElement element = new AttributeElement();
    element.add(key);
    if (match(ATTRIBUTE_OP)) {
      Node operator = new Anonymous(token);
      Node value = parseQuoted();
      if (value == null && match(IDENTIFIER)) {
        value = new Anonymous(token);
      }
      if (value != null) {
        element.add(operator);
        element.add(value);
      }
    }

    if (!seekIf(RIGHT_SQUARE_BRACKET)) {
      index = saveIndex;
      return null;
    }
    return element;
  }

  /**
   * Parse quoted string syntax, ignoring embedded curly variables.
   */
  private Quoted parseQuoted() {
    skipWs();
    int offset = 0;
    boolean escaped = false;

    // Check if this is the start of an escaped string.
    char ch = peek();
    if (ch == TILDE) {
      escaped = true;
      offset++;
    }

    // Check if this is the start of a quoted string.
    char delim = peek(offset);
    if (delim != APOSTROPHE && delim != QUOTATION_MARK) {
      return null;
    }

    Buffer buf = context.acquireBuffer();
    seek(offset + 1);
    while (index < length) {
      ch = peek();
      seek1();

      // Stop if we've just found the terminating delimiter or EOF
      if (ch == delim || ch == EOF) {
        break;
      }

      if (ch == LINE_FEED) {
        // Should be a serious error, but not sure this can even happen at this point.
        // Just avoid appending it for now.
        continue;
      }

      buf.append(ch);
      // Append all characters except backslash escape
      if (ch != '\\') {
        continue;
      }

      // Append the next character if not EOF, e.g. it may be an escaped delimiter.
      ch = peek();
      if (ch != EOF) {
        buf.append(ch);
        seek1();
      }
    }
    List<Node> parts = Arrays.<Node>asList(new Anonymous(buf.toString()));
    context.returnBuffer();
    return new Quoted(delim, escaped, parts);
  }

  private Selector initSelector(LessContext context, Selector selector) {
    return selector == null ? context.nodeBuilder().buildSelector() : selector;
  }
}
