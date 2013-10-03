package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;
import static com.squarespace.v6.template.less.model.NodeType.ALPHA;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Alpha extends BaseNode {
  
  private Node value;
  
  public Alpha(Node value) {
    if (value == null) {
      throw new IllegalArgumentException("Serious error: value cannot be null.");
    }
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
    return needsEval() ? new Alpha(value.eval(env)) : this;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Alpha) ? safeEquals(value, ((Alpha)obj).value) : false;
  }
  
  @Override
  public NodeType type() {
    return ALPHA;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append("alpha(opacity=");
    if (value != null) {
      value.repr(buf);
    }
    buf.append(')');
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    if (value != null) {
      buf.incrIndent();
      buf.indent();
      value.modelRepr(buf);
      buf.decrIndent();
    }
  }

}
