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

import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


/**
 * <p>
 * A Mixin is a macro that has a few function-like properties.  When a mixin
 * is called, the mixin is expanded into the calling scope.
 *</p>
 *
 *<p>
 * A mixin is not a proper function, as the mixin body's local scope is
 * intermingled with the caller's, so it can see variables in the caller's scope.
 * </p>
 *
 * <p>
 * The values of local variables depend heavily on the order of evaluation
 * inside the mixin body and can lead to confusing outcomes.
 * </p>
 */
public class Mixin extends BlockNode {

  /**
   * Name of the mixin.
   */
  protected final String name;

  /**
   * Mixin's parameters.
   */
  protected final MixinParams params;

  /**
   * Mixin's optional guard expression.
   */
  protected final Guard guard;

  /**
   * Closure attached to this mixin definition.
   */
  protected ExecEnv closure;

  /**
   * Number of times this mixin's body has been evaluated. Used to detect
   * and limit recursion.
   */
  protected int entryCount;

  /**
   * Constructs a mixin with the given name, parameters and guard expression.
   */
  public Mixin(String name, MixinParams params, Guard guard) {
    this(name, params, guard, new Block());
  }

  /**
   * Constructs a mixin with the given name, parameters and guard expression,
   * with the given block.
   */
  public Mixin(String name, MixinParams params, Guard guard, Block block) {
    super(block);
    this.name = name;
    this.params = params;
    this.guard = guard;
  }

  /**
   * Creates a copy of this mixin.
   */
  public Mixin copy() {
    Mixin result = new Mixin(name, params, guard, block.copy());
    result.copyBase(this);
    result.closure = closure;
    if (originalBlockNode != null) {
      result.originalBlockNode = originalBlockNode;
    }
    return result;
  }

  /**
   * Returns the mixin's name.
   */
  public String name() {
    return name;
  }

  /**
   * Returns the mixin's parameters.
   */
  public MixinParams params() {
    return params;
  }

  /**
   * Returns the mixin's guard expression.
   */
  public Guard guard() {
    return guard;
  }

  /**
   * Returns the number of times this mixin's body has been evaluated.
   */
  public int entryCount() {
    return entryCount;
  }

  /**
   * Begins evaluating the mixin's body.
   */
  public void enter() {
    entryCount++;
  }

  /**
   * Ends evaluating the mixin's body.
   */
  public void exit() {
    entryCount--;
  }

  /**
   * Returns the closure environment attached to the mixin's definition.
   */
  public ExecEnv closure() {
    return closure;
  }

  /**
   * Sets the closure on this mixin definition.
   */
  public void closure(ExecEnv env) {
    this.closure = env.copy();
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.MIXIN;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(name).append('(');
    if (params != null) {
      params.repr(buf);
    }
    buf.append(')');
    if (guard != null) {
      buf.append(" when ");
      guard.repr(buf);
    }
    buf.blockOpen();
    block.repr(buf);
    buf.blockClose();
    if (!buf.compress()) {
      buf.append('\n');
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent();

    if (params != null) {
      buf.indent();
      params.modelRepr(buf);
      buf.append('\n');
    }

    if (guard != null) {
      buf.indent();
      guard.modelRepr(buf);
      buf.append('\n');
    }

    buf.indent();
    super.modelRepr(buf);
    buf.decrIndent().append('\n');
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Mixin) {
      Mixin other = (Mixin)obj;
      return safeEquals(name, other.name)
          && safeEquals(params, other.params)
          && safeEquals(guard, other.guard);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
