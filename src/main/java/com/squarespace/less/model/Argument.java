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

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents an argument to a {@link MixinCall}. The argument may have an optional name.
 */
public class Argument implements Node {

  /**
   * Argument's name.
   */
  protected final String name;

  /**
   * Argument's value.
   */
  protected final Node value;

  /**
   * Constructs an argument with the given {@code value}.
   */
  public Argument(Node value) {
    this(null, value);
  }

  /**
   * Constructs a named argument with the given {@code value}.
   */
  public Argument(String name, Node value) {
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null");
    }
    this.name = name;
    this.value = value;
  }

  /**
   * Returns the argument's name, which may be null.
   */
  public String name() {
    return name;
  }

  /**
   * Returns the argument's value.
   */
  public Node value() {
    return value;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.ARGUMENT;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return value.needsEval();
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    return value.needsEval() ? new Argument(name, value.eval(env)) : this;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    if (name != null) {
      buf.append(name);
      buf.append(": ");
    }
    value.repr(buf);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    buf.append(type().toString()).append("\n");
    buf.incrIndent().indent();
    if (name != null) {
      buf.append(name);
    } else {
      buf.append("<null>");
    }

    buf.append('\n').indent();
    if (value != null) {
      value.modelRepr(buf);
    } else {
      buf.append("<null>");
    }
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Argument) {
      Argument other = (Argument)obj;
      return safeEquals(name, other.name) && safeEquals(value, other.value);
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
