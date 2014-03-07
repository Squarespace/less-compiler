package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


public class ValueElement extends Element {

  private final Node value;

  public ValueElement(Combinator comb, Node value) {
    super(comb);
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null");
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
    if (!needsEval()) {
      return this;
    }
    return new ValueElement(combinator(), value.eval(env));
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ValueElement) {
      ValueElement other = (ValueElement)obj;
      return combinator == other.combinator && safeEquals(value, other.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public void repr(Buffer buf) {
    value.repr(buf);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ');
    buf.append(combinator == null ? "<null>" : combinator.toString());
    buf.append(' ');
    value.modelRepr(buf);
  }

}
