package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessInternalException;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Assignment extends BaseNode {

  private String name;
  
  private Node value;
  
  public Assignment(String name, Node value) {
    if (name == null || value == null) {
      throw new LessInternalException("Serious error: name/value cannot be null.");
    }
    this.name = name;
    this.value = value;
  }
  
  public String name() {
    return name;
  }
  
  public Node value() {
    return value;
  }
  
  @Override
  public NodeType type() {
    return NodeType.ASSIGNMENT;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Assignment) {
      Assignment other = (Assignment)obj;
      return safeEquals(name, other.name) && safeEquals(value, other.value);
    }
    return false;
  }
  
  @Override
  public boolean needsEval() {
    return value.needsEval();
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    return value.needsEval() ? new Assignment(name, value.eval(env)) : this;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(name).append('=');
    value.repr(buf);
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(" name=").append(name).append('\n');
    if (value != null) {
      buf.incrIndent();
      buf.indent();
      value.modelRepr(buf);
      buf.decrIndent();
    }
  }

}
