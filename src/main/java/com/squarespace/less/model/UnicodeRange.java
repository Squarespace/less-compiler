package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.UNICODE_RANGE;

import com.squarespace.less.core.Buffer;


public class UnicodeRange extends BaseNode {
  
  private final String value;
  
  public UnicodeRange(String value) {
    this.value = value;
  }
  
  public String value() {
    return value;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof UnicodeRange) ? safeEquals(value, ((UnicodeRange)obj).value) : false; 
  }
  
  @Override
  public NodeType type() {
    return UNICODE_RANGE;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append(value);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('(').append(value).append(')');
  }
  
}
