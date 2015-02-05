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
import static com.squarespace.less.model.NodeType.SHORTHAND;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


/**
 * A shorthand expression, typically used in a font expression.
 *
 * This is a special node to distinguish the shorthand from division, since
 * they have the same appearance to the parser.
 */
public class Shorthand extends BaseNode {

  /**
   * Left-hand side of the shorthand expression.
   */
  protected final Node left;

  /**
   * Right-hand side of the shorthand expression.
   */
  protected final Node right;

  /**
   * Constructs a shorthand expression with the given left and right values.
   */
  public Shorthand(Node left, Node right) {
    this.left = left;
    this.right = right;
  }

  /**
   * Returns the left-hand value.
   */
  public Node left() {
    return left;
  }

  /**
   * Returns the right-hand value.
   */
  public Node right() {
    return right;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return left.needsEval() || right.needsEval();
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    return needsEval() ? new Shorthand(left.eval(env), right.eval(env)) : this;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return SHORTHAND;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    left.repr(buf);
    buf.append('/');
    right.repr(buf);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.incrIndent().append('\n').indent();
    left.modelRepr(buf);
    buf.append('\n').indent();
    right.modelRepr(buf);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Shorthand) {
      Shorthand other = (Shorthand)obj;
      return safeEquals(left, other.left) && safeEquals(right, other.right);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
