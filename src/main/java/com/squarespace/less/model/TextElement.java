package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.core.Buffer;


public class TextElement extends Element {

  private final String name;
  
  private final boolean isWildcard;
  
  public TextElement(Combinator comb) {
    super(comb);
    this.name = null;
    this.isWildcard = false;
  }
  
  public TextElement(Combinator comb, String name) {
    super(comb);
    this.name = name;
    this.isWildcard = name.equals("&");
  }

  public String name() {
    return name;
  }
 
  @Override
  public boolean isWildcard() {
    return isWildcard;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TextElement) {
      TextElement other = (TextElement)obj;
      return combinator == other.combinator && safeEquals(name, other.name);
    }
    return false;
  }
  
  @Override
  public void repr(Buffer buf) {
    if (name != null) {
      buf.append(name);
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ');
    buf.append(combinator == null ? "<null>" : combinator.toString());
    buf.append(' ').append(name);
  }
  
}
