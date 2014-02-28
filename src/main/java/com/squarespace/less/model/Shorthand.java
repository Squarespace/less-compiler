package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.SHORTHAND;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


public class Shorthand extends BaseNode {

  private final Node left;
  
  private final Node right;
  
  public Shorthand(Node left, Node right) {
    this.left = left;
    this.right = right;
  }
  
  public Node left() {
    return left;
  }
  
  public Node right() {
    return right;
  }
  
  @Override
  public boolean needsEval() {
    return left.needsEval() || right.needsEval();
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    return needsEval() ? new Shorthand(left.eval(env), right.eval(env)) : this;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Shorthand) {
      Shorthand other = (Shorthand)obj;
      return safeEquals(left, other.left) && safeEquals(right, other.right);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return SHORTHAND;
  }

  @Override
  public void repr(Buffer buf) {
    left.repr(buf);
    buf.append('/');
    right.repr(buf);
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.incrIndent().append('\n').indent();
    left.modelRepr(buf);
    buf.append('\n').indent();
    right.modelRepr(buf);
    buf.decrIndent();
  }
  
}
