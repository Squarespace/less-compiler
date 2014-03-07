package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;


public class Anonymous extends BaseNode {

  private final String value;

  public Anonymous(String value) {
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null.");
    }
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Anonymous) ? safeEquals(value, ((Anonymous)obj).value) : false;
  }

  @Override
  public NodeType type() {
    return NodeType.ANONYMOUS;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(value);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(" \"");
    if (value != null) {
      buf.append(StringEscapeUtils.escapeJava(value));
    }
    buf.append('"');
  }

}
