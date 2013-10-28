package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;
import static com.squarespace.v6.template.less.model.NodeType.COMMENT;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessInternalException;


public class Comment extends BaseNode {

  private String body;
  
  private boolean block;
  
  private boolean newline;
  
  public Comment(String body, boolean block) {
    this(body, block, false);
  }
  
  public Comment(String body, boolean block, boolean newline) {
    if (body == null) {
      throw new LessInternalException("Serious error: body cannot be null.");
    }
    this.body = body;
    this.block = block;
    this.newline = newline;
  }

  public String body() {
    return body;
  }

  public boolean block() {
    return block;
  }
  
  public boolean newline() {
    return newline;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Comment) {
      Comment other = (Comment)obj;
      return block == other.block && safeEquals(body, other.body);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return COMMENT;
  }
  
  @Override
  public void repr(Buffer buf) {
    if (block) {
      buf.append("/*").append(body).append("*/");
    } else {
      buf.append("//").append(body);
    }
    if (newline) {
      buf.append("\n");
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(block ? "block" : "single line");
    buf.append(' ').append(newline ? "newline" : "inline").append('\n');
    buf.incrIndent();
    buf.indent();
    buf.append(StringEscapeUtils.escapeJava(body));
    buf.decrIndent();
  }
  
}
