package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Parameter extends BaseNode {

  private String name;

  private Node value;
  
  private boolean variadic;
  
  public Parameter(String name) {
    this(name, null);
  }

  public Parameter(String name, Node value) {
    this.name = name;
    this.value = value;
    this.variadic = false;
  }
  
  public Parameter(String name, boolean variadic) {
    this.name = name;
    this.value = null;
    this.variadic = variadic;
  }

  public String name() {
    return name;
  }
  
  public Node value() {
    return value;
  }
  
  public boolean variadic() {
    return variadic;
  }

  @Override
  public boolean needsEval() {
    return value != null ? value.needsEval() : false;
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    return value == null ? new Parameter(name, variadic) : new Parameter(name, value.eval(env));
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Parameter) {
      Parameter other = (Parameter)obj;
      return variadic == other.variadic
          && safeEquals(name, other.name)
          && safeEquals(value, other.value);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.PARAMETER;
  }
  
  @Override
  public void repr(Buffer buf) {
    if (name != null) {
      buf.append(name);
      if (value != null) {
        buf.append(": ");
        value.repr(buf);
      } else if (variadic) {
        buf.append(" ...");
      }
    } else if (value != null) {
      value.repr(buf);
    } else if (variadic) {
      buf.append("...");
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(name);
    if (variadic) {
      buf.append(" [variadic]");
    }
    if (value != null) {
      buf.incrIndent().append('\n').indent();
      value.modelRepr(buf);
      buf.decrIndent();
    }
  }
  
}
