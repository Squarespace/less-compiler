package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class ValueElement extends Element {

  private Node value;
  
  public ValueElement(Combinator comb, Node value) {
    super(comb);
    if (value == null) {
      throw new IllegalArgumentException("Serious error: value cannot be null");
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
  public ValueElement copy() {
    return new ValueElement(combinator, value);
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
