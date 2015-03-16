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
import com.squarespace.less.exec.ExecEnv;


/**
 * Wraps another node in parenthesis.
 */
public class Paren extends BaseNode {

  /**
   * Value to be wrapped.
   */
  protected final Node value;

  /**
   * Wraps the given value in parenthesis.
   */
  public Paren(Node value) {
    this.value = value;
  }

  /**
   * Returns the wrapped value.
   */
  public Node value() {
    return value;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.PAREN;
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
    return value.needsEval() ? new Paren(value.eval(env)) : this;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append('(');
    value.repr(buf);
    buf.append(')');
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    if (value != null) {
      buf.incrIndent();
      buf.indent();
      value.modelRepr(buf);
      buf.decrIndent();
    }
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Paren) ? safeEquals(value, ((Paren)obj).value) : false;
  }

  @Override
  public int hashCode() {
    return hashCode == 0 ? buildHashCode(value) : hashCode;
  }

}
