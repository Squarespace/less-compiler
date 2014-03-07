package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.ALPHA;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


public class Alpha extends BaseNode {

  private final Node value;

  public Alpha(Node value) {
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null.");
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
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return ALPHA;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append("alpha(opacity=");
    value.repr(buf);
    buf.append(')');
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    buf.indent();
    value.modelRepr(buf);
    buf.decrIndent();
  }

}
