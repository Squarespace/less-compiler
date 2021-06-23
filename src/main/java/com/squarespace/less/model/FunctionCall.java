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


/**
 * Calls a named {@link Function}, if one has been defined, or just
 * emits something that looks like a function call.
 */
public class FunctionCall implements Node {

  /**
   * Name of the function.
   */
  protected final String name;

  /**
   * Arguments to the function.
   */
  protected List<Node> args;

  /**
   * Flag indicating whether
   */
  protected boolean evaluate;

  /**
   * Flag indicating whether or not this function is implemented in the
   * current compiler instance.
   */
  protected final boolean noImplementation;

  /**
   * Constructs a call to the function {@code name} with no arguments.
   */
  public FunctionCall(String name) {
    this(name, null, false);
  }

  /**
   * Constructs a call to the function {@code name} with the given {@code args}
   */
  public FunctionCall(String name, List<Node> args) {
    this(name, args, false);
  }

  /**
   * Constructs a call to the function {@code name} with the given {@code args}
   * and indicates that it is known the function has no implementation.
   */
  public FunctionCall(String name, List<Node> args, boolean noImplementation) {
    if (name == null) {
      throw new LessInternalException("Serious error: name cannot be null");
    }
    this.name = name;
    this.args = args;
    this.noImplementation = noImplementation;
    if (args != null) {
      int size = args.size();
      for (int i = 0; i < size; i++) {
        evaluate |= args.get(i).needsEval();
      }
    }
  }

  /**
   * Returns the name of the function to call.
   */
  public String name() {
    return name;
  }

  /**
   * Returns the arguments to the function call.
   */
  public List<Node> args() {
    return LessUtils.safeList(args);
  }

  /**
   * Adds an argument to the function call.
   */
  public void add(Node arg) {
    args = LessUtils.initList(args, 3);
    args.add(arg);
    evaluate |= arg.needsEval();
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return !noImplementation || evaluate;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (noImplementation) {
      return new FunctionCall(name, evaluate ? evalArgs(env) : args, true);
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

      // If we get null, fall through. Its a way for a function to signal
      // that its representation should be emitted, not executed.
    }

    // Function is not a built-in so render the function and its args.
    return new FunctionCall(name, evaluate ? evalArgs(env) : args, true);
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.FUNCTION_CALL;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
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

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" name=").append(name);
    if (noImplementation) {
      buf.append(" [no implementation]");
    }
    buf.append('\n').incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, args);
    buf.decrIndent();
  }

  /**
   * Evaluates the arguments to the function call.
   */
  private List<Node> evalArgs(ExecEnv env) throws LessException {
    List<Node> tempArgs = args();
    if (tempArgs.isEmpty()) {
      return tempArgs;
    }
    List<Node> res = new ArrayList<>(tempArgs.size());
    int size = tempArgs.size();
    for (int i = 0; i < size; i++) {
      Node arg = tempArgs.get(i);
      if (arg.needsEval()) {
        arg = arg.eval(env);
      }
      res.add(arg);
    }
    return res;
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
  public String toString() {
    return ModelUtils.toString(this);
  }

  @Override
  public int hashCode() {
    return ModelUtils.notHashable();
  }

}
