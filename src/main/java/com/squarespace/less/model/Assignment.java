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
 * Used to represent a "name = value" part of a function call.  An example of
 * this is the IE filter properties:
 *
 *   filter: progid:DXImageTransform.Microsoft.Alpha( opacity=50 )
 */
public class Assignment implements Node {

  /**
   * Assignment's name part.
   */
  protected final String name;

  /**
   * Assignment's value part.
   */
  protected final Node value;

  /**
   * Constructs an assignment with the given name and value.
   */
  public Assignment(String name, Node value) {
    if (name == null || value == null) {
      throw new LessInternalException("Serious error: name/value cannot be null.");
    }
    this.name = name;
    this.value = value;
  }

  /**
   * Returns the assignment's name.
   */
  public String name() {
    return name;
  }

  /**
   * Returns the assignment's value.
   */
  public Node value() {
    return value;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.ASSIGNMENT;
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
    return value.needsEval() ? new Assignment(name, value.eval(env)) : this;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(name).append('=');
    value.repr(buf);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" name=").append(name).append('\n');
    if (value != null) {
      buf.incrIndent();
      buf.indent();
      value.modelRepr(buf);
      buf.decrIndent();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Assignment) {
      Assignment other = (Assignment)obj;
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
