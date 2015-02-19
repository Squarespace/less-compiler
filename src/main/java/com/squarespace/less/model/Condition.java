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
import static com.squarespace.less.model.Operator.EQUAL;
import static com.squarespace.less.model.Operator.GREATER_THAN;
import static com.squarespace.less.model.Operator.GREATER_THAN_OR_EQUAL;
import static com.squarespace.less.model.Operator.LESS_THAN;
import static com.squarespace.less.model.Operator.LESS_THAN_OR_EQUAL;
import static com.squarespace.less.model.Operator.NOT_EQUAL;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.Constants;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.Comparison;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents one conditional clause in a {@link Guard}.
 */
public class Condition extends BaseNode {

  /**
   * Operator to apply to the left and right operands.
   */
  protected final Operator operator;

  /**
   * Left operand.
   */
  protected final Node left;

  /**
   * Right operand.
   */
  protected final Node right;

  /**
   * Whether to negate the result of the evaluation.
   */
  protected final boolean negate;

  /**
   * Constructs a conditional clause, applying the boolean operator to the left
   * and right operands.  Negates the result if the {@code negate} flag is set.
   */
  public Condition(Operator operator, Node left, Node right, boolean negate) {
    if (operator == null || left == null || right == null) {
      throw new LessInternalException("Serious error: operator/operands cannot be null.");
    }
    this.operator = operator;
    this.left = left;
    this.right = right;
    this.negate = negate;
  }

  /**
   * Returns the left operand.
   */
  public Node left() {
    return left;
  }

  /**
   * Returns the right operand.
   */
  public Node right() {
    return right;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return true;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    boolean result = negate ? !compare(env) : compare(env);
    return result ? Constants.TRUE : Constants.FALSE;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.CONDITION;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    if (negate) {
      buf.append("not ");
    }
    boolean nested = (left instanceof Condition) || (right instanceof Condition);
    if (!nested) {
      buf.append('(');
    }
    left.repr(buf);
    buf.append(' ').append(operator.toString()).append(' ');
    right.repr(buf);
    if (!nested) {
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
    buf.append(' ').append(operator.toString());
    if (negate) {
      buf.append(" [negate]");
    }
    buf.append('\n').incrIndent().indent();
    left.modelRepr(buf);
    buf.append('\n').indent();
    right.modelRepr(buf);
    buf.append('\n').decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Condition) {
      Condition other = (Condition)obj;
      boolean res = operator == other.operator
          && negate == other.negate
          && safeEquals(left, other.left)
          && safeEquals(right, other.right);
      return res;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * Executes the comparison of the operands.
  */
  private boolean compare(ExecEnv env) throws LessException {
    Node op0 = left.needsEval() ? left.eval(env) : left;
    Node op1 = right.needsEval() ? right.eval(env) : right;

    switch (operator) {
      case ADD:
      case DIVIDE:
      case MULTIPLY:
      case SUBTRACT:
        throw new LessException(ExecuteErrorMaker.expectedBoolOp(operator));

      case AND:
        return conjunction(env, op0, op1);

      case OR:
        return disjunction(env, op0, op1);

      default:
        break;
    }

    Comparison comparison = env.context().compare(op0, op1);
    boolean result = false;
    switch (comparison) {
      case LESS_THAN:
        result = operator == LESS_THAN || operator == LESS_THAN_OR_EQUAL || operator == NOT_EQUAL;
        break;

      case EQUAL_TO:
        result = operator == EQUAL || operator == LESS_THAN_OR_EQUAL || operator == GREATER_THAN_OR_EQUAL;
        break;

      case GREATER_THAN:
        result = operator == GREATER_THAN || operator == GREATER_THAN_OR_EQUAL || operator == NOT_EQUAL;
        break;

      case NOT_COMPARABLE:
      default:
        result = operator == NOT_EQUAL;
        break;
    }
    return result;
  }

  /**
   * Logical AND of the two operands.
   */
  private boolean conjunction(ExecEnv env, Node left, Node right) throws LessException {
    return truthValue(env, left) && truthValue(env, right);
  }

  /**
   * Logical OR of the two operands.
   */
  private boolean disjunction(ExecEnv env, Node left, Node right) throws LessException {
    return truthValue(env, left) || truthValue(env, right);
  }

  /**
   * Maps node types to a truth value based on LESS notion of "truthiness".
   */
  private boolean truthValue(ExecEnv env, Node node) throws LessException {
    switch (node.type()) {

      case ANONYMOUS:
      case KEYWORD:
      case QUOTED:
        return "true".equals(render(env, node));

      case TRUE:
        return true;

      default:
        return false;
    }
  }

  /**
   * Renders a node to get its string representation, for comparison purposes.
   */
  private String render(ExecEnv env, Node arg) throws LessException {
    if (arg instanceof Anonymous) {
      return ((Anonymous)arg).value();
    } else if (arg instanceof Keyword) {
      return ((Keyword)arg).value();
    }
    return env.context().render(arg);
  }

}
