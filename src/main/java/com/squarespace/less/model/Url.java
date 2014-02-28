package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a URL value within a CSS Rule.
 * 
 * Examples: url("http://nytimes.com")
 */
public class Url extends BaseNode {

  private final Node value;
  
  public Url(Node value) {
    this.value = value;
  }
  
  public Node value() {
    return value;
  }
  
  @Override
  public boolean needsEval() {
    return value.needsEval();
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    return needsEval() ? new Url(value.eval(env)) : this;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Url) ? safeEquals(value, ((Url)obj).value) : false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.URL;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append("url(");
    value.repr(buf);
    buf.append(')');
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.incrIndent().append('\n');
    if (value != null) {
      buf.indent();
      value.modelRepr(buf);
    }
    buf.decrIndent();
  }

}
