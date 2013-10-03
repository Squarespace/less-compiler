package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;
import static com.squarespace.v6.template.less.model.NodeType.KEYWORD;

import com.squarespace.v6.template.less.core.Buffer;


public class Keyword extends BaseNode {

  private String value;
  
  public Keyword(String value) {
    this.value = value;
  }
  
  public String value() {
    return value;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Keyword) ? safeEquals(value, ((Keyword)obj).value) : false;
  }
  
  @Override
  public NodeType type() {
    return KEYWORD;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append(value);
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(value);
  }
  
}
