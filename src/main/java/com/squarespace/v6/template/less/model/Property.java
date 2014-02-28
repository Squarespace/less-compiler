package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.core.Buffer;


public class Property extends BaseNode {

  private String name;
  
  public Property(String name) {
    this.name = name;
  }
  
  public String name() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Property) ? safeEquals(name, ((Property)obj).name) : false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.PROPERTY;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append(name);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(name);
  }
  
}
