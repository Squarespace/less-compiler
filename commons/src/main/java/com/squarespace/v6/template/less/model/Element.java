package com.squarespace.v6.template.less.model;



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
    throw new UnsupportedOperationException("You must implement equals(Object) in the subclass!");
  }
  
  @Override
  public NodeType type() {
    return NodeType.ELEMENT;
  }

}
