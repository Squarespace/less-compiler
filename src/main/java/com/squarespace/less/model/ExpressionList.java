package com.squarespace.less.model;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a comma-separated list of Expression.
 */
public class ExpressionList extends BaseNode {

  private List<Node> values;
  
  private boolean evaluate;
  
  public ExpressionList() {
  }
  
  public ExpressionList(Node node) {
    this.values = new ArrayList<>(2);
    this.values.add(node);
    evaluate |= node.needsEval();
  }
  
  public ExpressionList(List<Node> expressions) {
    this.values = expressions;
    for (Node node : expressions) {
      evaluate |= node.needsEval();
    }
  }
  
  public boolean isEmpty() {
    return size() == 0;
  }
  
  public int size() {
    return values == null ? 0 : values.size();
  }
  
  public void add(Node node) {
    values = LessUtils.initList(values, 2);
    this.values.add(node);
  }

  public List<Node> expressions() {
    return LessUtils.safeList(values);
  }

  @Override
  public boolean needsEval() {
    return evaluate;
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    ExpressionList result = new ExpressionList();
    for (Node node : expressions()) {
      result.add(node.eval(env));
    }
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof ExpressionList) 
        ? LessUtils.safeEquals(values, ((ExpressionList)obj).values) : false;
  }

  @Override
  public NodeType type() {
    return NodeType.EXPRESSION_LIST;
  }
  
  @Override
  public void repr(Buffer buf) {
    int size = values.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(',');
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
    buf.indent().append("\n");
  }
  
}
