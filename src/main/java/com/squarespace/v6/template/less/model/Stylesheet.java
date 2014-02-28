package com.squarespace.v6.template.less.model;

import com.squarespace.v6.template.less.core.Buffer;


public class Stylesheet extends BlockNode {
  
  public Stylesheet() {
    super();
  }
  
  public Stylesheet(Block block) {
    super(block);
  }
  
  public Stylesheet copy() {
    Stylesheet result = new Stylesheet(block().copy());
    result.charOffset = charOffset;
    result.lineOffset = lineOffset;
    return result;
  }
  
  @Override
  public NodeType type() {
    return NodeType.STYLESHEET;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Stylesheet) && super.equals(obj);
  }
  
  @Override
  public void repr(Buffer buf) {
    block.repr(buf);
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    super.modelRepr(buf);
    buf.decrIndent();
  }
  
}
