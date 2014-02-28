package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


public class MixinParams extends BaseNode {

  private List<Parameter> params;
  
  private boolean variadic;

  private int required;
  
  private boolean evaluate;
  
  public void add(Parameter param) {
    params = LessUtils.initList(params, 3);
    params.add(param);
    variadic |= param.variadic();
    Node paramValue = param.value();
    if (!param.variadic() && (param.name() == null || (param.name() != null && paramValue == null))) {
      required++;
    }
    if (paramValue != null) {
      evaluate |= paramValue.needsEval();
    }
  }
  
  public List<Parameter> params() {
    return LessUtils.safeList(params);
  }

  public int arity() {
    return params().size();
  }

  public int required() {
    return required;
  }
  
  public boolean variadic() {
    return variadic;
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
    MixinParams result = new MixinParams();
    for (Parameter param : params()) {
      result.add((Parameter)param.eval(env));
    }
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof MixinParams) ? safeEquals(params, ((MixinParams)obj).params) : false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.MIXIN_PARAMS;
  }
  
  @Override
  public void repr(Buffer buf) {
    if (params == null) {
      return;
    }
    int size = params.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      params.get(i).repr(buf);
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.incrIndent().append('\n');
    if (params == null || params.isEmpty()) {
      buf.indent().append("<empty>");
    } else {
      ReprUtils.modelRepr(buf, "\n", true, params);
    }
    buf.decrIndent();
  }
  
}
