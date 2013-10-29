package com.squarespace.v6.template.less.model;

import com.squarespace.v6.template.less.core.LessInternalException;


public abstract class Element extends BaseNode {
  
  protected Combinator combinator;

  public Element(Combinator comb) {
    this.combinator = comb;
  }
  
  public Combinator combinator() {
    return combinator;
  }
  
  public void setCombinator(Combinator comb) {
    this.combinator = comb;
  }

  public abstract boolean isWildcard();

  public abstract Element copy();
  
  @Override
  public boolean equals(Object obj) {
    throw new LessInternalException("Element subclass must implement equals(Object)!");
  }
  
  @Override
  public NodeType type() {
    return NodeType.ELEMENT;
  }

}
