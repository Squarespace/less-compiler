package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.EXPRESSION;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


public class Expression extends BaseNode {

  private List<Node> values;

  private boolean evaluate;

  public Expression() {
  }

  public Expression(List<Node> entities) {
    for (Node node : entities) {
      add(node);
    }
  }

  public void add(Node node) {
    values = LessUtils.initList(values, 2);
    values.add(node);
    evaluate |= node.needsEval();
  }

  public int size() {
    return (values == null) ? 0 : values.size();
  }

  public List<Node> values() {
    return LessUtils.safeList(values);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Expression) ? safeEquals(values, ((Expression)obj).values) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return EXPRESSION;
  }

  @Override
  public boolean needsEval() {
    return evaluate;
  }

  /**
   * Called when an expression is part of a variable definition.
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (values == null || !evaluate) {
      return this;
    }
    int size = values.size();
    if (size == 1) {
      return values.get(0).eval(env);
    }

    List<Node> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      result.add(values.get(i).eval(env));
    }
    return new Expression(result);
  }

  @Override
  public void repr(Buffer buf) {
    int size = values.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(' ');
      }
      values.get(i).repr(buf);
    }
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, values);
    buf.decrIndent();
  }

}
