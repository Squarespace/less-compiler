package com.squarespace.v6.template.less.model;

import java.math.BigDecimal;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.ExecuteErrorMaker;
import com.squarespace.v6.template.less.exec.ExecEnv;


public abstract class BaseNode implements Node {

  protected int lineOffset;
  
  protected int charOffset;
  
  protected void copyPosition(BaseNode from, BaseNode to) {
    to.setLineOffset(from.lineOffset);
    to.setCharOffset(from.charOffset);
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
      // Strip leading and trailing zeros and avoid scientific notation.
      String repr = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
      buf.append((value >= 0 && value < 1.0) ? repr.substring(1) : repr);
    }
  }

  @Override
  public int hashCode() {
    throw new RuntimeException("Serious error: model objects are not designed to be hashed.");
  }
  
  @Override
  public Node operate(Operator op, Node arg) throws LessException {
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
