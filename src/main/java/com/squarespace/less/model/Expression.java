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
import static com.squarespace.less.model.NodeType.EXPRESSION;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * A space-separated list of values.
 */
public class Expression implements Node {

  /**
   * List of values that make up the expression.
   */
  protected List<Node> values;

  /**
   * Flag indicating whether one or more of the values needs to be evaluated.
   */
  protected boolean evaluate;

  /**
   * Constructs an empty expression.
   */
  public Expression() {
  }

  /**
   * Constructs an empty expression with a list of starting values.
   */
  public Expression(List<Node> values) {
    int size = values.size();
    for (int i = 0; i < size; i++) {
      Node node = values.get(i);
      add(node);
    }
  }

  /**
   * Adds a node to the list of values.
   */
  public void add(Node node) {
    values = LessUtils.initList(values, 2);
    values.add(node);
    evaluate |= node.needsEval();
  }

  /**
   * Returns the values in the expression.
   */
  public List<Node> values() {
    return LessUtils.safeList(values);
  }

  /**
   * Returns the number of values in the expression.
   */
  public int size() {
    return (values == null) ? 0 : values.size();
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return EXPRESSION;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return evaluate;
  }

  /**
   * Called when an expression is part of a variable definition.
   *
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (values == null || !evaluate) {
      return this;
    }
    int size = values.size();
    if (size == 1) {
      return values.get(0).eval(env);
    }

    List<Node> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      result.add(values.get(i).eval(env));
    }
    return new Expression(result);
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    if (values != null) {
      int size = values.size();
      for (int i = 0; i < size; i++) {
        if (i > 0) {
          buf.append(' ');
        }
        values.get(i).repr(buf);
      }
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
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, values);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Expression) ? safeEquals(values, ((Expression)obj).values) : false;
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
