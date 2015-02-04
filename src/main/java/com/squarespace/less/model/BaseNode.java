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

import java.math.BigDecimal;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.exec.ExecEnv;


public abstract class BaseNode implements Node {

  protected int lineOffset;

  protected int charOffset;

  protected Object userData;

  public Object userData() {
    return userData;
  }

  public void userData(Object userData) {
    this.userData = userData;
  }

  public void copyBase(BaseNode from) {
    setLineOffset(from.lineOffset);
    setCharOffset(from.charOffset);
    userData(from.userData);
  }

  public int lineOffset() {
    return lineOffset;
  }

  public void setLineOffset(int lineOffset) {
    this.lineOffset = lineOffset;
  }

  public int charOffset() {
    return charOffset;
  }

  public void setCharOffset(int charOffset) {
    this.charOffset = charOffset;
  }

  public void typeRepr(Buffer buf) {
    buf.append(type().toString());
  }

  public void posRepr(Buffer buf) {
    buf.append(" [").append(lineOffset).append(',').append(charOffset).append("]");
  }

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

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("Serious error: model objects are not designed to be hashed.");
  }

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

  @Override
  public String toString() {
    Buffer buf = new Buffer(4);
    modelRepr(buf);
    return buf.toString();
  }

  @Override
  public String repr() {
    Buffer buf = new Buffer(2);
    repr(buf);
    return buf.toString();
  }

  @Override
  public void repr(Buffer buf) {
    buf.append("<no repr for " + type().toString() + ">");
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append("<not implemented>");
  }

}
