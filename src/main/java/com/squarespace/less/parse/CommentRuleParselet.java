package com.squarespace.less.parse;

import com.squarespace.less.model.Node;


/**
 * Special parser that handles standalone comments (not embedded in a rule). Newline
 * handling is slightly different.
 */
public class CommentRuleParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    return CommentParselet.parseComment(stm, true);
  }
  
}
