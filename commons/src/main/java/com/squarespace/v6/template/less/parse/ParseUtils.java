package com.squarespace.v6.template.less.parse;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.core.FlexList;
import com.squarespace.v6.template.less.model.ParseError;


public class ParseUtils {

  /**
   * Build a user-readable parser error message, showing the exact context for
   * the error. We append this to the given exception inside a ParseError node.
   */
  public static void parseError(LessException exc, String raw, int index) {
    Stream stm = new Stream(raw);
    List<int[]> offsets = new ArrayList<>();

    // Search for the line that contains our error index.
    int charPos = 0;
    while (stm.peek() != Chars.EOF) {
      int start = stm.position();
      charPos = index - start;
      stm.seekTo(Chars.LINE_FEED);
      int end = stm.position();
      offsets.add(new int[] { start, end });
      if (end > index) {
        break;
      }
    }

    Buffer buf = new Buffer(6);
    // Select the last N lines.
    int size = offsets.size();
    int start = Math.max(0, size - 5);
    for (int i = start; i < size; i++) {
      int[] pos = offsets.get(i);
      position(buf, i + 1, 4);
      buf.append(raw.substring(pos[0], pos[1]));
    }
    
    // Position an arrow at the offending character position
    indent(buf, 7);
    for (int i = 0; i < charPos; i++) {
      buf.append('.');
    }
    buf.append("^\n");
    
    ParseError error = new ParseError();
    error.errorMessage(buf.toString());
    exc.push(error);
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

  
  public static String errorMessage(LessException exc, Mark pos, String raw, FlexList<String> stack) {
    String message = exc.getMessage() + " " + pos;
//    int index = pos.index;
//    String window = StringEscapeUtils.escapeJava(raw.substring(index, Math.min(raw.length(), index + 40)));

    // Traverse the stack, rendering the header of each parsed block we entered.
    Buffer buf = new Buffer(4);
    int size = stack.size();
    for (int i = 0; i < size; i++) {
      buf.indent().append(stack.get(i)).append(" {\n");
      buf.incrIndent();
    }
    return message + "\n" + buf.toString();
  }
  
}
