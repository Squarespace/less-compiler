package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.model.Node;


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
