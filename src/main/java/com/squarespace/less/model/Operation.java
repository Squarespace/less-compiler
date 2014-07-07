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
 * TODO: when javascript output is implemented, expressions, operations and function calls
 * should be translated into javascript calls so that they can be evaluated late.
 * Ex:
 *   ['font', [dim(12, 'px') + tweak('@fontIncrease'), tweak('@backgroundColor'), ] ]
 *
 * produces:   font: 14px #ff0301;
 */
public class Operation extends BaseNode {

  protected final Operator operator;

  protected final Node left;

  protected final Node right;

  public Operation(Operator operator, Node left, Node right) {
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  public Node left() {
    return left;
  }

  public Node right() {
    return right;
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

  @Override
  public NodeType type() {
    return NodeType.OPERATION;
  }

  @Override
  public boolean needsEval() {
    return true;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    Node op0 = left.needsEval() ? left.eval(env) : left;
    Node op1 = right.needsEval() ? right.eval(env) : right;

    // Check if we can cast the node to a friendlier type.
    op0 = cast(op0);
    op1 = cast(op1);

    LessOptions opts = env.context().options();
    if (op0.is(NodeType.DIMENSION) && op1.is(NodeType.COLOR)) {
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

  @Override
  public void repr(Buffer buf) {
    buf.append('(');
    left.repr(buf);
    buf.append(' ').append(operator.toString()).append(' ');
    right.repr(buf);
    buf.append(')');
  }

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

  private Node cast(Node node) {
    switch (node.type()) {
      case KEYWORD:
        Keyword kwd = (Keyword)node;
        RGBColor color = RGBColor.fromName(kwd.value());
        return color == null ? kwd : color;

      default:
        return node;
    }
  }

}
