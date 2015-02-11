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
 * A set of rules nested under one or more {@link Selector}s.
 */
public class Ruleset extends BlockNode {

  /**
   * Selector set for this ruleset.
   */
  protected final Selectors selectors;

  /**
   * Flag indicating this ruleset is being evaluated. Helps detect recursion.
   */
  protected boolean evaluating;

  /**
   * Indicates whether this ruleset has at least one mixin path.
   */
  protected boolean hasMixinPath;

  /**
   * Constructs an empty ruleset with empty selectors.
   */
  public Ruleset() {
    this.selectors = new Selectors();
  }

  /**
   * Constructs an empty ruleset with the initial selector set.
   */
  public Ruleset(Selectors selectors) {
    this(selectors, new Block());
  }

  /**
   * Constructs an empty ruleset with the initial selector set, and an
   * initial block.
   */
  public Ruleset(Selectors selectors, Block block) {
    super(block);
    this.selectors = selectors;
    this.hasMixinPath = selectors.hasMixinPath();
  }

  /**
   * Evaluates the ruleset's selectors and returns a copy.
   */
  public Ruleset copy(ExecEnv env) throws LessException {
    Ruleset result = new Ruleset((Selectors)selectors.eval(env), block.copy());
    result.fileName = fileName;
    if (originalBlockNode != null) {
      result.originalBlockNode = originalBlockNode;
    }
    return result;
  }

  /**
   * Returns the ruleset's selector set.
   */
  public Selectors selectors() {
    return selectors;
  }

  /**
   * Indicates whether the ruleset's selectors has at least one mixin path.
   */
  public boolean hasMixinPath() {
    return hasMixinPath;
  }

  /**
   * Marks the ruleset as being evaluated.
   */
  public void enter() {
    evaluating = true;
  }

  /**
   * Clears the ruleset's evaluation flag.
   */
  public void exit() {
    evaluating = false;
  }

  /**
   * Indicates if the ruleset is currently being evaluated.
   */
  public boolean evaluating() {
    return evaluating;
  }

  /**
   * Add a node to the ruleset.
   */
  @Override
  public void add(Node node) {
    if (node instanceof Selector) {
      Selector selector = (Selector)node;
      selectors.add(selector);
      hasMixinPath |= selectors.hasMixinPath();

    } else {
      super.add(node);
    }
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.RULESET;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    selectors.repr(buf);
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
    buf.append('\n');
    buf.incrIndent().indent();
    selectors.modelRepr(buf);
    buf.append('\n');
    buf.indent();
    super.modelRepr(buf);
    buf.decrIndent().append('\n');
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Ruleset) {
      Ruleset other = (Ruleset)obj;
      return safeEquals(selectors, other.selectors) && super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}

