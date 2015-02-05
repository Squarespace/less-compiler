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

import static com.squarespace.less.core.ExecuteErrorMaker.badColorMath;
import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.LessErrorInfo;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;

/**
 * Represents an operator being applied to a pair of operands.
 */
public class Operation extends BaseNode {

  /**
   * Operator to be applied to the operands.
   */
  protected final Operator operator;

  /**
   * Left-hand operand.
   */
  protected final Node left;

  /**
   * Right-hand operand.
   */
  protected final Node right;

  /**
   * Constructs an operation for the given operator and operands.
   */
  public Operation(Operator operator, Node left, Node right) {
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  /**
   * Returns the left-hand operand.
   */
  public Node left() {
    return left;
  }

  /**
   * Returns the right-hand operand.
   */
  public Node right() {
    return right;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.OPERATION;
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
    Node op0 = left.needsEval() ? left.eval(env) : left;
    Node op1 = right.needsEval() ? right.eval(env) : right;

    // Check if we can cast the node to a friendlier type.
    op0 = cast(op0);
    op1 = cast(op1);

    LessOptions opts = env.context().options();
    if ((op0 instanceof Dimension) && (op1 instanceof BaseColor)) {
      if (operator == Operator.MULTIPLY || operator == Operator.ADD) {
        Node temp = op0;
        op0 = op1;
        op1 = temp;

      } else {
        LessErrorInfo info = badColorMath(operator, op0);
        if (opts.strict()) {
          throw new LessException(info);
        } else if (!opts.hideWarnings()) {
          env.addWarning(info.getMessage() + ".. ignoring right-hand operand.");
        }
        return op0;
      }
    }

    return op0.operate(env, operator, op1);
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append('(');
    left.repr(buf);
    buf.append(' ').append(operator.toString()).append(' ');
    right.repr(buf);
    buf.append(')');
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(operator.toString()).append('\n');
    buf.incrIndent();
    buf.indent();
    left.modelRepr(buf);
    buf.append('\n').indent();
    right.modelRepr(buf);
    buf.decrIndent();
  }

  /**
   * Casts the node to a more specific type.
   */
  private Node cast(Node node) {
    if (node instanceof Keyword) {
      Keyword kwd = (Keyword)node;
      RGBColor color = RGBColor.fromName(kwd.value());
      return color == null ? kwd : color;
    }
    return node;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Operation) {
      Operation other = (Operation)obj;
      return operator == other.operator
          && safeEquals(left, other.left)
          && safeEquals(right, other.right);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
