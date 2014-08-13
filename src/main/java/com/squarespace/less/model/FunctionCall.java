/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;


public class FunctionCall extends BaseNode {

  protected final String name;

  protected List<Node> args;

  protected boolean evaluate;

  protected final boolean noimpl;

  public FunctionCall(String name) {
    this(name, null, false);
  }

  public FunctionCall(String name, List<Node> args) {
    this(name, args, false);
  }

  public FunctionCall(String name, List<Node> args, boolean noimpl) {
    if (name == null) {
      throw new LessInternalException("Serious error: name cannot be null");
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
      // that it should be emitted, not executed. This happens in the context()
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
  public int hashCode() {
    return super.hashCode();
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
          buf.listSep();
        }
        args.get(i).repr(buf);
      }
    }
    buf.append(')');
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
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
