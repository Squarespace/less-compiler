package com.squarespace.v6.template.less.model;


public class False extends Keyword {

  public False() {
    super("false");
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof False ? true : super.equals(obj);
  }
 
  @Override
  public NodeType type() {
    return NodeType.FALSE;
  }
 
}
