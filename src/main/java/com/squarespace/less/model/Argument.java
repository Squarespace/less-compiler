package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


public class Argument extends BaseNode {

  private final String name;
  
  private final Node value;
    
  public Argument(Node value) {
    this(null, value);
  }
  
  public Argument(String name, Node value) {
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null");
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
    return NodeType.ARGUMENT;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Argument) {
      Argument other = (Argument)obj;
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
    return value.needsEval() ? new Argument(name, value.eval(env)) : this;
  }
  
  @Override
  public void repr(Buffer buf) {
    if (name != null) {
      buf.append(name);
      buf.append(": ");
    }
    value.repr(buf);
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    buf.append(type().toString()).append("\n");
    buf.incrIndent().indent();
    if (name != null) {
      buf.append(name);
    } else {
      buf.append("<null>");
    }

    buf.append('\n').indent();
    if (value != null) {
      value.modelRepr(buf);
    } else {
      buf.append("<null>");
    }
    buf.decrIndent();
  }
  
}
