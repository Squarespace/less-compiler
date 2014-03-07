package com.squarespace.less.model;

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.LessUtils.safeEquals;

import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * List of Condition that follow a Mixin.
 */
public class Guard extends BaseNode {

  private List<Condition> conditions;

  public Guard() {
  }

  public void add(Condition cond) {
    conditions = LessUtils.initList(conditions, 2);
    conditions.add(cond);
  }

  public void addAll(Condition ... elements) {
    conditions = Arrays.asList(elements);
  }

  public List<Condition> conditions() {
    return LessUtils.safeList(conditions);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Guard) {
      Guard other = (Guard)obj;
      return safeEquals(conditions, other.conditions);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean needsEval() {
    return true;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    Node result = FALSE;
    for (Condition condition : conditions()) {
      result = condition.eval(env);
      if (result.is(NodeType.TRUE)) {
        break;
      }
    }
    return result;
  }

  @Override
  public NodeType type() {
    return NodeType.GUARD;
  }

  @Override
  public void repr(Buffer buf) {
    int size = conditions.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      conditions.get(i).repr(buf);
    }
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    if (conditions != null) {
      ReprUtils.modelRepr(buf, "\n", true, conditions);
    } else {
      buf.append("<null>");
    }
    buf.decrIndent();
  }

}
