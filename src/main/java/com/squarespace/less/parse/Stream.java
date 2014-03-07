package com.squarespace.less.parse;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.CharPattern;
import com.squarespace.less.core.Chars;


/**
 * Wraps a String and provides an interface for interacting with the sequence of characters.
 */
public class Stream {

  protected static final boolean DEBUG = false;

  protected final String raw;

  protected final int length;

  protected int index;

  protected int furthest;

  protected int lineOffset;

  protected int charOffset;

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

  protected void dump() {
    char ch = (index >= length) ? Chars.EOF : raw.charAt(index);
    String esc = StringEscapeUtils.escapeJava(ch + "");
    System.out.printf("Stream: index=%d len=%d line=%d char=%d ch=\"%s\"\n",
          index, length, lineOffset, charOffset, esc);
  }

  public String raw() {
    return raw;
  }

  public int position() {
    return index;
  }

  public Mark mark() {
    Mark pos = new Mark();
    mark(pos);
    return pos;
  }

  /**
   * Mark current position in the stream so that we can restore it later.
   */
  public void mark(Mark mark) {
    mark.index = index;
    mark.lineOffset = lineOffset;
    mark.charOffset = charOffset;
  }

  /**
   * Restore the stream to the marked position.
   */
  public int restore(Mark mark) {
    index = mark.index;
    lineOffset = mark.lineOffset;
    charOffset = mark.charOffset;
    return index;
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
      if (!CharClass.whitespace(curr)) {
        break;
      }
      consume(curr);
      index++;
    }
    // Important not to update 'furthest' pointer when skipping whitespace
    return index - start;
  }

  public int skipEmpty() {
    int start = index;
    while (index < length) {
      char curr = raw.charAt(index);
      if (!CharClass.skippable(curr)) {
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
    return "Stream(\"" + StringEscapeUtils.escapeJava(remainder()) + "\")";
  }

  public boolean complete() {
    return index == length;
  }

  public String remainder() {
    return raw.substring(index);
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
  }

  /**
   * Useful for debugging the parser.
   */
  @SuppressWarnings("unused")
  private void stack() {
    StackTraceElement[] elems = Thread.currentThread().getStackTrace();
    for (int i = 1; i < 5; i++) {
      System.out.println(elems[i].getLineNumber() + " " + elems[i].getFileName() + " " + elems[i].getMethodName());
    }
    System.out.println();
  }

}
