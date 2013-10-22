package com.squarespace.v6.template.less.model;

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
  
  private boolean noimpl;
  
  public FunctionCall(String name) {
    this(name, null, false);
  }
  
  public FunctionCall(String name, List<Node> args) {
    this(name, args, false);
  }

  public FunctionCall(String name, List<Node> args, boolean noimpl) {
    if (name == null) {
      throw new IllegalArgumentException("Serious error: name cannot be null");
    }
    this.name = name;
    this.args = args;
    this.noimpl = noimpl;
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
    return !noimpl || evaluate;
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (noimpl) {
      return evaluate ? new FunctionCall(name, evalArgs(env), true) : this;
    }
    
    // Check if this function is built-in.
    Function func = env.context().findFunction(name);
    if (func != null) {
      // Invoke built-in function
      List<Node> values = evalArgs(env);
      Node result = null;
      func.spec().validate(env, func, values);
      result = func.invoke(env, values);
      if (result != null) {
        return result;
      }
      
      // If we get null, fall through. Its a way for a function impl to signal
      // that it should be emitted, not executed. This hapepns in the context()
      // function -- it checks its args, and if the first arg is not of the expected
      // type, it returns null, indicating that the function call's repr should
      // be emitted, not an evaluated result.
    }
    
    // Function is not a built-in so render the function and its args.
    return evaluate ? new FunctionCall(name, evalArgs(env), true) : this;
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
    if (args != null) {
      int size = args.size();
      for (int i = 0; i < size; i++) {
        if (i > 0) {
          buf.append(", ");
        }
        args.get(i).repr(buf);
      }
    }
    buf.append(')');
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(" name=").append(name);
    if (noimpl) {
      buf.append(" [no implementation]");
    }
    buf.append('\n').incrIndent();
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
