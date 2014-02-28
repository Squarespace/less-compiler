package com.squarespace.less.model;


public class True extends Keyword {

  public True() {
    super("true");
  }
  
  @Override
  public boolean equals(Object obj) {
    return obj instanceof True ? true : super.equals(obj);
  }
  
  @Override
  public NodeType type() {
    return NodeType.TRUE;
  }
  
}
