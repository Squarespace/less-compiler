package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.ExecuteErrorType.INVALID_OPERATION2;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public abstract class BaseNode implements Node {

  protected int lineOffset;
  
  protected int charOffset;
  
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
    if (lineOffset != 0 && charOffset != 0) {
      buf.append(" [").append(lineOffset).append(',').append(charOffset).append("]");
    }
  }

  @Override
  public int hashCode() {
    throw new RuntimeException("Serious error: model objects are not designed to be hashed.");
  }
  
  @Override
  public Node operate(Operator op, Node arg) throws LessException {
    NodeType argType = (arg == null) ? null : arg.type();
    throw new LessException(error(INVALID_OPERATION2).type(op).arg0(type()).arg1(argType));
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
  public void repr(Buffer buf) {
    buf.append("<no repr for " + type().toString() + ">");
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append("<not implemented>");
  }
  
}
