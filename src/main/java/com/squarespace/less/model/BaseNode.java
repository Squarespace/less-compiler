package com.squarespace.less.model;

import java.math.BigDecimal;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.exec.ExecEnv;


public abstract class BaseNode implements Node {

  protected int lineOffset;

  protected int charOffset;

  public void copyPosition(BaseNode from) {
    setLineOffset(from.lineOffset);
    setCharOffset(from.charOffset);
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
  public final int hashCode() {
    throw new UnsupportedOperationException("Serious error: model objects are not designed to be hashed.");
  }

  @Override
  public Node operate(ExecEnv env, Operator op, Node arg) throws LessException {
    NodeType argType = (arg == null) ? null : arg.type();
    throw new LessException(ExecuteErrorMaker.invalidOperation(op, type(), argType));
  }

  @Override
  public boolean needsEval() {
    return false;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    return this;
  }

  @Override
  public boolean is(NodeType type) {
    return type == type();
  }

  @Override
  public String toString() {
    Buffer buf = new Buffer(4);
    modelRepr(buf);
    return buf.toString();
  }

  @Override
  public String repr() {
    Buffer buf = new Buffer(0);
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
    buf.append("<not implemented>");
  }

}
