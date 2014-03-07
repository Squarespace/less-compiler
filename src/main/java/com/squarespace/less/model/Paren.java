package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


public class Paren extends BaseNode {

  private final Node node;

  public Paren(Node node) {
    this.node = node;
  }

  public Node node() {
    return node;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Paren) ? safeEquals(node, ((Paren)obj).node) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.PAREN;
  }

  @Override
  public boolean needsEval() {
    return node.needsEval();
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    return node.needsEval() ? new Paren(node.eval(env)) : this;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append('(');
    node.repr(buf);
    buf.append(')');
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    if (node != null) {
      buf.incrIndent();
      buf.indent();
      node.modelRepr(buf);
      buf.decrIndent();
    }
  }

}
