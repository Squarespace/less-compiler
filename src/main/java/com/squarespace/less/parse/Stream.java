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

import static com.squarespace.less.core.CharClass.CLASSIFIER;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.less.core.CharPattern;
import com.squarespace.less.core.Chars;


/**
 * Wraps a String and provides an interface for interacting with the sequence of characters.
 */
public class Stream {

  public static final int MARK_DIM = 4;
  public static final int FLAG_OPENSPACE = 1;
  protected static final boolean DEBUG = false;
  protected final String raw;
  protected final int length;
  protected int index;
  protected int furthest;
  protected int lineOffset;
  protected int charOffset;
  private int flags = 0;

  // We replace the Mark type with the following int[4] stack.
  // Ideally we could stack-allocate our Mark objects, since they're
  // scoped to a Java stack frame, but Java only allows heap allocation
  // of these objects at the moment.
  protected int marksptr = 0;
  private int capacity = 32;
  private int[][] marks = new int[capacity][MARK_DIM];

  public Stream(String raw) {
    this.raw = raw;
    this.length = raw.length();
  }

  public int getLineOffset() {
    return lineOffset;
  }

  public int getCharOffset() {
    return charOffset;
  }

//  protected void dump() {
//    char ch = (index >= length) ? Chars.EOF : raw.charAt(index);
//    String esc = StringEscapeUtils.escapeJava(ch + "");
//    System.out.printf("Stream: index=%d len=%d line=%d char=%d ch=\"%s\"\n",
//          index, length, lineOffset, charOffset, esc);
//  }

  /**
   * Mark current position in the stream so that we can restore it later.
   */
  public int[] mark() {
    // check if we need to grow the marker depth
    if (marksptr == capacity) {
      int sz = capacity * 2;
      int[][] newmarks = new int[sz][MARK_DIM];
      System.arraycopy(marks, 0, newmarks, 0, capacity);
      this.marks = newmarks;
      capacity = sz;
    }

    // Record the mark
    int[] m = marks[marksptr];
    m[0] = index;
    m[1] = lineOffset;
    m[2] = charOffset;
    m[3] = flags;
    marksptr++;
    return m;
  }

  /**
   * Restore the stream to the marked position.
   */
  public int restore(int[] m) {
    this.index = m[0];
    this.lineOffset = m[1];
    this.charOffset = m[2];
    this.flags = m[3];
    return this.index;
  }

  /**
   * Pop a marked position off the stack.
   */
  public void popMark() {
    marksptr--;
    if (marksptr < 0) {
      marksptr = 0;
    }
  }

//  private void log(String what) {
//    StackTraceElement[] elems = Thread.currentThread().getStackTrace();
//    String where = elems[3].getClassName() + "." + elems[3].getMethodName();
//    System.out.println(what + ": " + where);
//  }

  public String raw() {
    return raw;
  }

  public int position() {
    return index;
  }

  public boolean hasFlag(int flag) {
    return (flags & flag) != 0;
  }

  /**
   * Return the character under the stream pointer. Does not increment the
   * stream pointer.
   */
  public char peek() {
    return (index >= length) ? Chars.EOF : raw.charAt(index);
  }

  /**
   * Return the character at position 'index + offset'. Does not increment the
   * stream pointer.
   */
  public char peek(int offset) {
    int pos = index + offset;
    return (pos < 0 || pos >= length) ? Chars.EOF : raw.charAt(pos);
  }

  /**
   * Seek ahead in the stream 'offset' characters. Increments the stream pointer and
   * increments the line/offset counters.
   */
  public char seek(int offset) {
    int limit = Math.min(length, index + offset);
    while (index < limit) {
      consume(raw.charAt(index));
      index++;
    }
    furthest = Math.max(index, furthest);
    return peek();
  }

  public char seek1() {
    if (index < length) {
      consume(raw.charAt(index));
      index++;
    }
    furthest = Math.max(index, furthest);
    return peek();
  }

  /**
   * Seek past a block open/close character, or other character that closes a
   * rule-level token, and set the FLAG_OPENSPACE flag. This indicates
   * to the ElementParselet that we've effectively entered "open space", so
   * the combinator can be set to DESC even if previous character is not
   * whitespace.
   */
  public void seekOpenSpace() {
    seek1();
    setOpenSpace();
  }

  public void setOpenSpace() {
    flags |= FLAG_OPENSPACE;
  }

  /**
   * Seek ahead until 'ch' is found. Increments the stream pointer, leaving it pointing at
   * the character just after 'ch', or the end of the string if not matched.
   */
  public void seekTo(char ch) {
    while (index < length) {
      char curr = raw.charAt(index);
      consume(curr);
      index++;
      if (curr == ch) {
        break;
      }
    }
    furthest = Math.max(index, furthest);
  }

  /**
   * If the character under the cursor equals 'ch', consume it and return
   * true; else return false;
   */
  public boolean seekIf(char ch) {
    if (peek() == ch) {
      seek1();
      return true;
    }
    return false;
  }

  /**
   * Consume all whitespace characters, positioning the pointer over the next non-whitespace character.
   * Returns the number of characters skipped.
   */
  public int skipWs() {
    int start = index;
    while (index < length) {
      char curr = raw.charAt(index);
      if (!CLASSIFIER.whitespace(curr)) {
        break;
      }
      if (curr == Chars.LINE_FEED) {
        lineOffset++;
        charOffset = 0;
      } else {
        charOffset++;
      }
      index++;
    }
    flags &= ~FLAG_OPENSPACE;

    // Important not to update 'furthest' pointer when skipping whitespace
    return index - start;
  }

  public int skipEmpty() {
    int start = index;
    while (index < length) {
      char curr = raw.charAt(index);
      if (!CLASSIFIER.skippable(curr)) {
        break;
      }
      consume(curr);
      index++;
    }
    // Important not to update 'furthest' pointer when skipping 'empty' chars.
    return index - start;
  }

  /**
   * Searches the stream to find the character pattern, consuming every character
   * it sees along the way.  Searching the string "ABCDEFGH" for the pattern "DE",
   * this would position the cursor over the 'F'.
   *
   * Searching for a pattern P in a string S of length N, this runs O(N).
   */
  public boolean seek(CharPattern table) {
    char[] pattern = table.pattern();
    int[] next = table.next();
    int patternLen = pattern.length;
    int j;
    for (j = 0; index < length && j < patternLen; index++) {
      char ch = raw.charAt(index);
      consume(ch);
      while (j >= 0 && ch != pattern[j]) {
        j = next[j];
      }
      j++;
    }
    furthest = Math.max(index, furthest);
    return (j == patternLen);
  }


  @Override
  public String toString() {
    return "Stream(\"" + StringEscapeUtils.escapeJava(raw.substring(index)) + "\")";
  }

  public String furthest() {
    return raw.substring(Math.min(furthest, length - 1));
  }

  private void consume(char ch) {
    if (ch == Chars.LINE_FEED) {
      lineOffset++;
      charOffset = 0;
    } else {
      charOffset++;
    }
    flags &= ~FLAG_OPENSPACE;
  }

  /**
   * Useful for debugging the parser.
   */
//  @SuppressWarnings("unused")
//  private void stack() {
//    StackTraceElement[] elems = Thread.currentThread().getStackTrace();
//    for (int i = 1; i < 5; i++) {
//      System.out.println(elems[i].getLineNumber() + " " + elems[i].getFileName() + " " + elems[i].getMethodName());
//    }
//    System.out.println();
//  }

}
