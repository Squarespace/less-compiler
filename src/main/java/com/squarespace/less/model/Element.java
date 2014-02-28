package com.squarespace.less.model;

import com.squarespace.less.core.LessInternalException;


public abstract class Element extends BaseNode {
  
  protected final Combinator combinator;

  public Element(Combinator comb) {
    this.combinator = comb;
  }
  
  public Combinator combinator() {
    return combinator;
  }
  
  public abstract boolean isWildcard();

  @Override
  public boolean equals(Object obj) {
    throw new LessInternalException("Element subclass must implement equals(Object)!");
  }
  
  @Override
  public NodeType type() {
    return NodeType.ELEMENT;
  }

}
