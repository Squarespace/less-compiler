package com.squarespace.less.parse;

import com.squarespace.less.core.CharPattern;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Node;


/**
 * Parse both single line '//' and block '/*' comments.
 */
public class CommentParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    return parseComment(stm, false);
  }

  protected static Node parseComment(LessStream stm, boolean ruleLevel) {
    char ch = stm.peek();

    // Check for a comment start sequence, "//" or "/*".
    if (ch != Chars.SLASH) {
      return null;
    }

    ch = stm.peek(1);
    boolean block = (ch == Chars.ASTERISK);
    boolean comment = (ch == Chars.SLASH) || block;
    if (!comment) {
      return null;
    }

    // Skip over "//" or "/*".
    stm.seek(2);
    int start = stm.index;

    // A comment without an ending sequence ends with EOF.
    int end = stm.length;

    // Match a comment ending sequence, "\n" or "*/"
    CharPattern pattern = (block) ? Patterns.BLOCK_COMMENT_END : Patterns.LINE_COMMENT_END;
    if (stm.seek(pattern)) {
      end = stm.index - pattern.length();
    }

    return new Comment(stm.raw.substring(start, end), block, ruleLevel);
  }

}
