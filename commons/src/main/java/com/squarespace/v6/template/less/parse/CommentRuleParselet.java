package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.model.Node;


public class CommentRuleParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    return CommentParselet.parseComment(stm, true);
  }
  
}
