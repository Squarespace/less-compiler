package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.Constants.FALSE;
import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import java.util.Arrays;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.exec.ExecEnv;


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
    return (obj instanceof Guard) ? safeEquals(conditions, ((Guard)obj).conditions) : false;
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
