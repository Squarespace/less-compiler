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

import static com.squarespace.less.core.ExecuteErrorMaker.rulesetExpression;

import java.math.BigDecimal;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.exec.ExecEnv;


/**
 * Common behaviors for all nodes.
 */
public abstract class BaseNode implements Node {

  /**
   * Line offset where this node was defined.
   */
  protected int lineOffset;

  /**
   * Character offset of the line where this node was defined.
   */
  protected int charOffset;

  /**
   * User data attached to this node
   */
  protected Object userData;

  /**
   * Returns any user data attached to this node.
   */
  public Object userData() {
    return userData;
  }

  /**
   * Attaches the user data to this node.
   */
  public void userData(Object userData) {
    this.userData = userData;
  }

  /**
   * Copies the base field values of {@code from} to this node.
   */
  public void copyBase(BaseNode from) {
    setLineOffset(from.lineOffset);
    setCharOffset(from.charOffset);
    userData(from.userData);
  }

  /**
   * Returns the line offset where this node was defined.
   */
  public int lineOffset() {
    return lineOffset;
  }

  /**
   * Sets the line offset where this node was defined.
   */
  public void setLineOffset(int lineOffset) {
    this.lineOffset = lineOffset;
  }

  /**
   * Returns the character offset of the line where this node was defined.
   */
  public int charOffset() {
    return charOffset;
  }

  /**
   * Sets the character offset of the line where this node was defined.
   */
  public void setCharOffset(int charOffset) {
    this.charOffset = charOffset;
  }

  /**
   * Constructs the type representation for this node.
   */
  public void typeRepr(Buffer buf) {
    buf.append(type().toString());
  }

  /**
   * Constructs the position representation for this node.
   */
  public void posRepr(Buffer buf) {
    buf.append(" [").append(lineOffset + 1).append(',').append(charOffset + 1).append("]");
  }

  /**
   * Helper method to format a double value and append it to the buffer.
   */
  public static void formatDouble(Buffer buf, double value) {
    long lval = (long)value;
    if (value == lval) {
      buf.append(lval);
    } else {
      // Strip trailing zeros and avoid scientific notation.
      String repr = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
      // Strip leading zeros for positive and negative numbers.
      if (value >= 0 && value < 1.0) {
        buf.append(repr.substring(1));
      } else if (value > -1.0 && value < 0) {
        buf.append('-').append(repr.substring(2));
      } else {
        buf.append(repr);
      }
    }
  }

  /**
   * Raises an exception, as nodes are not currently hashable.
   *
   * @throws  UnsupportedOperationException
   */
  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("Serious error: model objects are not designed to be hashed.");
  }

  /**
   * Implemented by nodes that participate in operations. Throws an exception otherwise.
   *
   * See {@link Node#operate(ExecEnv, Operator, Node)}
   */
  @Override
  public Node operate(ExecEnv env, Operator op, Node arg) throws LessException {
    NodeType argType = (arg == null) ? null : arg.type();
    throw new LessException(ExecuteErrorMaker.invalidOperation(op, type(), argType));
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return false;
  }

  /**
   * See {@link Node#eval(ExecEnv env)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    return this;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public String repr() {
    Buffer buf = new Buffer(2);
    repr(buf);
    return buf.toString();
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append("<no repr for " + type().toString() + ">");
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append("<not implemented>");
  }

  @Override
  public String toString() {
    Buffer buf = new Buffer(4);
    modelRepr(buf);
    return buf.toString();
  }

  /**
   * Check if the node is a {@link BlockNode} and is participating in an expression.
   */
  protected Node blockExpressionCheck(Node node) throws LessException {
    if (node instanceof BlockNode) {
      throw new LessException(rulesetExpression());
    }
    return node;
  }

}
