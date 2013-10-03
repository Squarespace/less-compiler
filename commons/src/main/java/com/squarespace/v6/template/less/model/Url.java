package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


/**
 * Represents a URL value within a CSS Rule.
 * 
 * Examples: url("http://nytimes.com")
 */
public class Url extends BaseNode {

  private Node value;
  
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
