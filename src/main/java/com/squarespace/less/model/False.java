package com.squarespace.less.model;


public class False extends Keyword {

  public False() {
    super("false");
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof False ? true : super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.FALSE;
  }

}
