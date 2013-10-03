package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.ExecuteErrorType.FUNCTION_CALL;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;


public class FunctionCall extends BaseNode {

  private String name;
  
  private List<Node> args;

  private boolean evaluate;
  
  public FunctionCall(String name) {
    this(name, null);
  }
  
  public FunctionCall(String name, List<Node> args) {
    if (name == null) {
      throw new IllegalArgumentException("Serious error: name cannot be null");
    }
    this.name = name;
    this.args = args;
  }
  
  public String name() {
    return name;
  }
  
  public List<Node> args() {
    return LessUtils.safeList(args);
  }
  
  public void add(Node arg) {
    args = LessUtils.initList(args, 3);
    args.add(arg);
    evaluate |= arg.needsEval();
  }
  
  @Override
  public boolean needsEval() {
    return true;
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    Function func = env.context().findFunction(name);
    if (func != null) {
      // Invoke built-in function
      List<Node> values = evalArgs(env);
      Node result = null;
      try {
        func.spec().validate(func, values);
        result = func.invoke(env, values);
      } catch (LessException e) {
        throw new LessException(error(FUNCTION_CALL).name(name).arg0(e.getMessage()));
      }
      if (result != null) {
        return result;
      }
      
      // If we get null, fall through. Its a way for a function impl to signal
      // that it should be emitted, not executed.
    }
    
    // Function is not a built-in so render the function and its args.
    return evaluate ? new FunctionCall(name, evalArgs(env)) : this;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FunctionCall) {
      FunctionCall other = (FunctionCall)obj;
      return safeEquals(name, other.name) && safeEquals(args, other.args);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.FUNCTION_CALL;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append(name).append('(');
    int size = args.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      args.get(i).repr(buf);
    }
    buf.append(')');
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(" name=").append(name).append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, args);
    buf.decrIndent();
  }
  
  private List<Node> evalArgs(ExecEnv env) throws LessException {
    List<Node> tempArgs = args();
    if (tempArgs.isEmpty()) {
      return tempArgs;
    }
    List<Node> res = new ArrayList<>(tempArgs.size());
    for (Node arg : tempArgs) {
      if (arg.needsEval()) {
        arg = arg.eval(env);
      }
      res.add(arg);
    }
    return res;
  }

}
