package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.core.Buffer;


public class Property extends BaseNode {

  private final String name;

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
