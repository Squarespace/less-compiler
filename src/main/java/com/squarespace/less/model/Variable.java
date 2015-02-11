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

import static com.squarespace.less.core.ExecuteErrorMaker.invalidRulesetReference;
import static com.squarespace.less.core.ExecuteErrorMaker.varUndefined;
import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.VARIABLE;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a reference to a variable.
 */
public class Variable extends BaseNode {

  private static final int INDIRECT = 0x01;

  private static final int CURLY = 0x02;

  private static final int RULESET = 0x04;

  /**
   * Name of the variable.
   */
  protected final String name;

  /**
   * Flags set on this variable reference.
   */
  protected final int flags;

  /**
   * Construct a variable reference with the given name.
   */
  public Variable(String name) {
    this(name, false, false);
  }

  /**
   * Construct a variable reference with the given name, and indicate whether
   * it is inside a {@link Quoted} string, and whether this variable
   * references a detached {@link Ruleset}.
   */
  public Variable(String name, boolean curly, boolean ruleset) {
    int flags = (curly ? CURLY : 0) | (ruleset ? RULESET : 0);
    if (name == null) {
      throw new LessInternalException("Serious error: name cannot be null");
    }
    if (name.startsWith("@@")) {
      name = name.substring(1);
      flags |= INDIRECT;
    }
    this.name = name;
    this.flags = flags;
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
    return (flags & INDIRECT) != 0;
  }

  /**
   * Indicates whether this variable reference is inside a {@link Quoted} string.
   */
  public boolean curly() {
    return (flags & CURLY) != 0;
  }

  /**
   * Indicates whether this variable references a detached {@link Ruleset}.
   */
  public boolean ruleset() {
    return (flags & RULESET) != 0;
  }

  /**
   * Traverses the variable reference, to get its value.
   */
  protected Node dereference(Definition def, ExecEnv env) throws LessException {
    Node result = def.dereference(env);

    // Check if this variable is a detached ruleset reference.
    if (ruleset()) {
      if (result instanceof Block) {
        return result;
      }
      throw new LessException(invalidRulesetReference(name, result.type()));
    }

    if (!indirect()) {
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
    if (indirect()) {
      buf.append('@');
    }
    buf.append('@');
    if (curly()) {
      buf.append('{');
    }
    buf.append(name.substring(1));
    if (curly()) {
      buf.append('}');
    }
    if (ruleset()) {
      buf.append("()");
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(indirect() ? "@" + name : name);
    if (curly()) {
      buf.append(" (curly)");
    }
    if (ruleset()) {
      buf.append(" (detached ruleset)");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Variable) {
      Variable other = (Variable)obj;
      return flags == other.flags && safeEquals(name, other.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
