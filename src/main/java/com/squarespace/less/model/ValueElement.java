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
 * Part of a selector which wraps a value.
 */
public class ValueElement extends SelectorPart {

  /**
   * The element's value.
   */
  protected final Node value;

  public ValueElement(Node value) {
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null");
    }
    this.value = value;
  }

  /**
   * Returns the element's value.
   */
  public Node value() {
    return value;
  }

  @Override
  public NodeType type() {
    return NodeType.VALUE_ELEMENT;
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
    if (needsEval()) {
      return new ValueElement(value.eval(env));
    }
    return this;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    boolean quoted = (value instanceof Quoted);
    // TODO: handle forward-compatibility with 2.x syntax
    if (quoted) {
      buf.append('(');
    }
    value.repr(buf);
    if (quoted) {
      buf.append(')');
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ');
    value.modelRepr(buf);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ValueElement) {
      return safeEquals(value, ((ValueElement)obj).value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hashCode == 0 ? buildHashCode(value) : hashCode;
  }

}
