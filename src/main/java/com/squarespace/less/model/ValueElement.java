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
 * An {@link Element} which wraps a value.
 */
public class ValueElement extends Element {

  /**
   * The element's value.
   */
  protected final Node value;

  public ValueElement(Combinator comb, Node value) {
    super(comb);
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
    if (!needsEval()) {
      return this;
    }
    return new ValueElement(combinator(), value.eval(env));
  }

  /**
   * See {@link Element#isWildcard()}
   */
  @Override
  public boolean isWildcard() {
    return false;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    boolean quoted = (value instanceof Quoted);
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
    buf.append(combinator == null ? "<null>" : combinator.toString());
    buf.append(' ');
    value.modelRepr(buf);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ValueElement) {
      ValueElement other = (ValueElement)obj;
      return combinator == other.combinator && safeEquals(value, other.value);
    }
    return false;
  }


}
