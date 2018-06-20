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

import static com.squarespace.less.core.ExecuteErrorMaker.varUndefined;
import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.VARIABLE;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a reference to a variable.
 */
public class Variable extends BaseNode {

  /**
   * Name of the variable.
   */
  protected final String name;

  /**
   * Indicates whether the variable is an indirect reference.
   */
  protected final boolean indirect;

  /**
   * Indicates whether this variable reference is inside a {@link Quoted}.
   */
  protected final boolean curly;

  /**
   * Construct a variable reference with the given name.
   */
  public Variable(String name) {
    this(name, false);
  }

  /**
   * Construct a variable reference with the given name, and indicate whether
   * it is inside a {@link Quoted} string.
   */
  public Variable(String name, boolean curly) {
    if (name == null) {
      throw new LessInternalException("Serious error: name cannot be null");
    }
    if (name.startsWith("@@")) {
      name = name.substring(1);
      indirect = true;
    } else {
      indirect = false;
    }
    this.name = name;
    this.curly = curly;
  }

  /**
   * Return the name of the variable reference.
   */
  public String name() {
    return name;
  }

  /**
   * Indicates whether this is an indirect reference.
   */
  public boolean indirect() {
    return indirect;
  }

  /**
   * Indicates whether this variable reference is inside a {@link Quoted} string.
   */
  public boolean curly() {
    return curly;
  }

  /**
   * Traverses the variable reference, to get its value.
   */
  protected Node dereference(Definition def, ExecEnv env) throws LessException {
    Node result = def.dereference(env);
    if (!indirect) {
      return result;
    }

    // Render the node to obtain the new variable name and eval that. We render
    // the value as if it were inside a string.
    LessContext ctx = env.context();
    Buffer buf = ctx.newBuffer();
    buf.startDelim('"');
    ctx.render(buf, result);
    return env.context().nodeBuilder().buildVariable("@" + buf.toString()).eval(env);
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return true;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    Definition def = env.resolveDefinition(name);
    if (def == null) {
      throw new LessException(varUndefined(name));
    }
    if (def.evaluating()) {
      throw new LessException(ExecuteErrorMaker.varCircularRef(name));
    }
    return dereference(def, env);
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return VARIABLE;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    if (indirect) {
      buf.append('@');
    }
    buf.append('@');
    if (curly) {
      buf.append('{');
    }
    buf.append(name.substring(1));
    if (curly) {
      buf.append('}');
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(indirect ? "@" + name : name);
    if (curly) {
      buf.append(" (curly)");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Variable) {
      Variable other = (Variable)obj;
      return indirect == other.indirect
          && curly == other.curly
          && safeEquals(name, other.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
