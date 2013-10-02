package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.FlexList;


public class ParseUtils {

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
