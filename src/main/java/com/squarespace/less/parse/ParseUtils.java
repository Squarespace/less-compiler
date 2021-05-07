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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.ParseError;


public class ParseUtils {


  // TODO: future, more efficient location of error line in source
  // holding off on this for now as it could produce slight differences in error
  // messages which would cause error test cases to break. once the new parser
  // is established we can take care of this.

//  private static List<int[]> findErrorOffsets(String raw, int furthest, int lines) {
//    System.out.println("string length: " + raw.length());
//    List<int[]> offsets = new ArrayList<>();
//    int index = furthest;
//    while (index > 0 && lines > 0) {
//      int j = raw.lastIndexOf('\n', index - 1);
//      if (j == -1) {
//        break;
//      }
//      System.out.println("newline: " + j);
//      offsets.add(new int[] { j + 1, index});
//      index = j;
//      lines--;
//    }
//    Collections.reverse(offsets);
//    return offsets;
//
//    System.out.println(offsets.stream().map(e -> Arrays.toString(e)).collect(Collectors.toList()));
//
//    for (int[] offset : offsets) {
//      System.out.println(StringEscapeUtils.escapeJava(raw.substring(offset[0], offset[1])));
//    }
//    return offsets;
//  }

  private static final int WINDOW_SIZE = 74;

  private static class Stream {

    final String raw;
    final int length;
    int pos = 0;

    Stream(String raw) {
      this.raw = raw;
      this.length = raw.length();
    }

    char peek() {
      return (pos >= length) ? Chars.EOF : raw.charAt(pos);
    }

    void seekTo(char ch) {
      while (pos < length) {
        char c = raw.charAt(pos);
        pos++;
        if (c == ch) {
          break;
        }
      }
    }
  }

  /**
   * Build a user-readable parser error message, showing the exact context for
   * the error. We append this to the given exception inside a ParseError node.
   */
  public static LessException parseError(LessException exc, Path filePath, String raw, int index) {
    List<int[]> offsets = new ArrayList<>();
    Stream stm = new Stream(raw);

    // Search for the line that contains our error index.
    int charPos = 0;

    while (stm.peek() != Chars.EOF) {
      int start = stm.pos;
      charPos = index - start;
      stm.seekTo('\n');
      int end = stm.pos;

      offsets.add(new int[] { start, end });

      // Stop when we've found the line that contains the error
      if (end > index) {
        break;
      }
    }

    // Select the last N lines we collected.
    Buffer buf = new Buffer(6);
    int size = offsets.size();
    int start = Math.max(0, size - 5);
    for (int i = start; i < size; i++) {
      int[] pos = offsets.get(i);
      position(buf, i + 1, 4);

      // Last line has special handling. We want to position the error in the middle
      // of the line, so for extremely long lines we need to shift things over.
      if (i + 1 == size) {
        int len = pos[1] - pos[0];
        if (len > WINDOW_SIZE) {
          int errpos = pos[0] + charPos;
          int skip = (int)Math.floor(WINDOW_SIZE / 2.0);
          int leftpos = Math.max(errpos - skip, pos[0]);
          charPos -= leftpos - pos[0] - 4;
          buf.append("... ");
          buf.append(raw.substring(leftpos, Math.min(leftpos + WINDOW_SIZE, pos[1])));

        } else {
          buf.append(raw.substring(pos[0], pos[1]));
        }

      } else {
        buf.append(compressString(raw.substring(pos[0], pos[1])));
      }
    }

    // Position an arrow at the offending character position
    if (buf.prevChar() != '\n') {
      buf.append('\n');
    }
    indent(buf, 7);
    for (int i = 0; i < charPos; i++) {
      buf.append('.');
    }
    buf.append("^\n");

    ParseError error = new ParseError();
    error.filePath(filePath);
    error.errorMessage(buf.toString());
    exc.push(error);
    return exc;
  }

  private static void indent(Buffer buf, int width) {
    for (int i = 0; i < width; i++) {
      buf.append(' ');
    }
    buf.indent();
  }

  private static int position(Buffer buf, int line, int colWidth) {
    String pos = Integer.toString(line);
    int width = colWidth - pos.length();
    for (int i = 0; i < width; i++) {
      buf.append(' ');
    }
    buf.append(pos).append("   ");
    return pos.length();
  }

  public static String compressString(String value) {
    int len = value.length();
    if (len <= WINDOW_SIZE) {
      return value;
    }

    /* Calculate the number of visible characters and maximum segment size then:
     *  (a) if the segment size is <= 10 chars, place the ellipses at the end
     *  (b) otherwise, place the ellipses in the middle
   . */
    StringBuilder buf = new StringBuilder(WINDOW_SIZE);
    int visible = WINDOW_SIZE - 4;
    int segSize = (int)Math.floor(visible / 2.0);
    return buf.append(value.substring(0, visible - segSize))
        .append(" ... ")
        .append(value.substring(len - segSize, len))
        .toString();
  }

}
