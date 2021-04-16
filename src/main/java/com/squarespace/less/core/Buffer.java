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

package com.squarespace.less.core;

import static com.squarespace.less.core.CharClass.CLASSIFIER;

/**
 * General-purpose buffer. Wraps a {@link StringBuilder} with some
 * LESS-specific methods.
 */
public class Buffer {

  public static final int DEFAULT_PRECISION = 8;

  /**
   * Internal buffer.
   */
  private final StringBuilder buf = new StringBuilder();

  /**
   * Number of spaces of indent.
   */
  private final int indentSize;

  /**
   * Flag to remove unnecessary whitespace, e.g. "minify".
   */
  private final boolean compress;

  /**
   * Delimiter used for start/end of escaped output.
   */
  private char delim = Chars.EOF;

  /**
   * Previous character in the buffer.
   */
  private char prev = Chars.LINE_FEED;

  /**
   * Current block nesting level.
   */
  private int indentCurr;

  /**
   *
   */
  private int numericScale;

  /**
   * Constructs a buffer with the given indent size, with whitespace compression disabled.
   */
  public Buffer(int indentSize) {
    this(indentSize, false, DEFAULT_PRECISION);
  }

  /**
   * Constructs a buffer with the given indent size and whitespace compression.
   */
  public Buffer(int indentSize, boolean compress) {
    this(indentSize, compress, DEFAULT_PRECISION);
  }

  /**
   * Constructs a buffer with the given indent size, whitespace compression, and numeric precision.
   */
  public Buffer(int indentSize, boolean compress, int numericScale) {
    this.indentSize = indentSize;
    this.compress = compress;
    this.numericScale = numericScale;
  }

  /**
   * Return a fresh buffer with the same initialization options as this one.
   */
  public Buffer newBuffer() {
    return new Buffer(indentSize, compress, numericScale);
  }

  /**
   * Indicates if whitespace compression is enabled.
   */
  public boolean compress() {
    return compress;
  }

  /**
   * Returns the maximum scale for decimal rounding.
   */
  public int numericScale() {
    return this.numericScale;
  }

  /**
   * Current length of the internal buffer.
   */
  public int length() {
    return buf.length();
  }

  /**
   * Resets the buffer to the initial state.
   */
  public void reset() {
    if (buf.length() != 0) {
      buf.setLength(0);
    }
    delim = Chars.EOF;
    prev = Chars.LINE_FEED;
  }

  /**
   * Increments the indentation level.
   */
  public Buffer incrIndent() {
    this.indentCurr += indentSize;
    return this;
  }

  /**
   * Decrements the indentation level.
   */
  public Buffer decrIndent() {
    this.indentCurr -= indentSize;
    return this;
  }

  /**
   * Resets the indentation level to zero.
   */
  public Buffer resetIndent() {
    this.indentCurr = 0;
    return this;
  }

  /**
   * Indicates if the buffer is in escape mode.
   */
  public boolean inEscape() {
    return delim != Chars.EOF;
  }

  /**
   * Returns the previous character in the buffer.
   */
  public char prevChar() {
    return prev;
  }

  /**
   * Emits whitespace to indent the next line of output.
   */
  public Buffer indent() {
    for (int i = 0; i < indentCurr; i++) {
      buf.append(' ');
    }
    if (indentCurr != 0) {
      prev = ' ';
    }
    return this;
  }

  /**
   * Enter escape mode with the given delimiter.
   */
  public Buffer startDelim(char ch) {
    if (inEscape()) {
      throw new LessInternalException("Serious error: buffer already in escape mode for " + delim);
    }
    this.delim = ch;
    return this;
  }

  /**
   * Exits escape mode.
   */
  public Buffer endDelim() {
    this.delim = Chars.EOF;
    return this;
  }

  /**
   * Appends a {@code long} to the buffer.
   */
  public Buffer append(long num) {
    buf.append(num);
    prev = buf.charAt(buf.length() - 1);
    return this;
  }

  /**
   * Appends a {@code double} to the buffer.
   */
  public Buffer append(double num) {
    buf.append(num);
    prev = buf.charAt(buf.length() - 1);
    return this;
  }

  /**
   * Appends a single {@code char} to the buffer.
   */
  public Buffer append(char ch) {
    buf.append(ch);
    prev = ch;
    return this;
  }

  /**
   * Appends a {@code String} to the buffer.
   */
  public Buffer append(String str) {
    if (str == null) {
      return this;
    }
    int len = str.length();
    if (len == 0) {
      return this;
    }
    buf.append(str);
    prev = str.charAt(len - 1);
    return this;
  }

  /**
   * Opens a nested block (left curly bracket) with optional whitespace suppression.
   */
  public void blockOpen() {
    if (!compress && !CLASSIFIER.whitespace(prev)) {
      append(' ');
    }
    append('{');
    if (!compress) {
      append('\n');
      incrIndent();
    }
  }

  /**
   * Closes a nested block (right curly bracket) with optional whitespace suppression.
   */
  public void blockClose() {
    if (compress) {
      append('}');
    } else {
      decrIndent();
      indent();
      append("}\n");
    }
  }

  /**
   * Emits a list separator (comma) with optional whitespace suppression.
   */
  public Buffer listSep() {
    if (compress) {
      append(',');
    } else {
      append(", ");
    }
    return this;
  }

  /**
   * Emits the character that ends a rule (semicolon) with optional whitespace suppression.
   */
  public void ruleEnd() {
    append(';');
    if (!compress) {
      append('\n');
    }
  }

  /**
   * Emits the separator between a property and value in a rule (colon) with optional
   * whitespace suppression.
   */
  public void ruleSep() {
    if (compress) {
      append(':');
    } else {
      append(": ");
    }
  }

  /**
   * Emits a selector separator (comma) with optional whitespace suppression.
   */
  public void selectorSep() {
    if (compress) {
      append(',');
    } else {
      append(",\n");
    }
  }

  @Override
  public String toString() {
    return buf.toString();
  }

}
