package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.ExecuteErrorMaker.badColorMath;
import static com.squarespace.v6.template.less.core.ExecuteErrorMaker.incompatibleUnits;
import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.ErrorInfo;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;

/**
 * TODO: when javascript output is implemented, expressions, operations and function calls
 * should be translated into javascript calls so that they can be evaluated late.
 * Ex:
 *   ['font', [dim(12, 'px') + tweak('@fontIncrease'), tweak('@backgroundColor'), ] ]
 *
 * produces:   font: 14px #ff0301;
 */
public class Operation extends BaseNode {

  private Operator operator;
  
  private Node operand0;
  
  private Node operand1;
  
  public Operation(Operator operator, Node operand0, Node operand1) {
    this.operator = operator;
    this.operand0 = operand0;
    this.operand1 = operand1;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Operation) {
      Operation other = (Operation)obj;
      return operator == other.operator
          && safeEquals(operand0, other.operand0)
          && safeEquals(operand1, other.operand1);
    }
    return false;
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
    Node op0 = operand0.needsEval() ? operand0.eval(env) : operand0;
    Node op1 = operand1.needsEval() ? operand1.eval(env) : operand1;

    // Check if we can cast the node to a friendlier type.
    op0 = cast(op0);
    op1 = cast(op1);
    
    Options opts = env.context().options();
    if (op0.is(NodeType.DIMENSION) && op1.is(NodeType.COLOR)) {
      if (operator == Operator.MULTIPLY || operator == Operator.ADD) {
        Node temp = op0;
        op0 = op1;
        op1 = temp;
        
      } else {
        ErrorInfo info = badColorMath(operator, op0);
        if (opts.strict()) {
          throw new LessException(info);
        } else if (!opts.hideWarnings()) {
          env.addWarning(info.getMessage() + ".. ignoring right-hand operand.");
        }
        return op0;
      }
    }

    // Dimensions that have units cannot be added/multiplied with a color.
    if (op0.is(NodeType.COLOR) && op1.is(NodeType.DIMENSION)) {
      Dimension dim = (Dimension)op1;
      if (dim.unit() != null) {
        ErrorInfo info = incompatibleUnits(dim.unit(), "a color");
        if (opts.strict()) {
          throw new LessException(info);
        } else if (!opts.hideWarnings()) {
          env.addWarning(info.getMessage() + ".. stripping unit.");
        }
        op1 = new Dimension(dim.value());
      }
    }
    return op0.operate(env, operator, op1);
  }

  @Override
  public void repr(Buffer buf) {
    buf.append('(');
    operand0.repr(buf);
    buf.append(' ').append(operator.toString()).append(' ');
    operand1.repr(buf);
    buf.append(')');
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(operator.toString()).append('\n');
    buf.incrIndent();
    buf.indent();
    operand0.modelRepr(buf);
    buf.append('\n').indent();
    operand1.modelRepr(buf);
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
