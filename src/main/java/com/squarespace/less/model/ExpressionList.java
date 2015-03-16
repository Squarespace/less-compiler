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

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a comma-separated list of {@link Expression} nodes.
 */
public class ExpressionList extends BaseNode {

  /**
   * List of values in the expression list.
   */
  protected List<Node> values;

  /**
   * Flag indicating whether any of the values require evaluation.
   */
  protected boolean evaluate;

  /**
   * Constructs an empty expression list.
   */
  public ExpressionList() {
  }

  /**
   * Constructs an expression list with a single initial value.
   */
  public ExpressionList(Node node) {
    this.values = new ArrayList<>(2);
    this.values.add(node);
    evaluate |= node.needsEval();
  }

  /**
   * Constructs an expression list with the given initial values.
   * @param expressions
   */
  public ExpressionList(List<Node> expressions) {
    this.values = expressions;
    for (Node node : expressions) {
      evaluate |= node.needsEval();
    }
  }

  /**
   * Returns the number of values in the expression list.
   */
  public int size() {
    return values == null ? 0 : values.size();
  }

  /**
   * Adds a value to the expression list.
   */
  public void add(Node node) {
    values = LessUtils.initList(values, 2);
    this.values.add(node);
  }

  /**
   * Returns the list of values in the expression list.
   */
  public List<Node> expressions() {
    return LessUtils.safeList(values);
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.EXPRESSION_LIST;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return evaluate;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    ExpressionList result = new ExpressionList();
    for (Node node : expressions()) {
      node = node.eval(env);
      result.add(blockExpressionCheck(node));
    }
    return result;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    int size = values.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      values.get(i).repr(buf);
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
    buf.indent().append("\n");
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof ExpressionList) ? safeEquals(values, ((ExpressionList)obj).values) : false;
  }

  @Override
  public int hashCode() {
    return hashCode == 0 ? buildHashCode(values) : hashCode;
  }

}
