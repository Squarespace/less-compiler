package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;
import static com.squarespace.v6.template.less.model.Operator.EQUAL;
import static com.squarespace.v6.template.less.model.Operator.GREATER_THAN;
import static com.squarespace.v6.template.less.model.Operator.GREATER_THAN_OR_EQUAL;
import static com.squarespace.v6.template.less.model.Operator.LESS_THAN;
import static com.squarespace.v6.template.less.model.Operator.LESS_THAN_OR_EQUAL;
import static com.squarespace.v6.template.less.model.Operator.NOT_EQUAL;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.Constants;
import com.squarespace.v6.template.less.core.ExecuteErrorMaker;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Condition extends BaseNode {

  private final Operator operator;
  
  private final Node operand0;
  
  private final Node operand1;
  
  private final boolean negate;
  
  public Condition(Operator operator, Node operand0, Node operand1, boolean negate) {
    if (operator == null || operand0 == null || operand1 == null) {
      throw new IllegalArgumentException("Serious error: operator/operands cannot be null.");
    }
    this.operator = operator;
    this.operand0 = operand0;
    this.operand1 = operand1;
    this.negate = negate;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Condition) {
      Condition other = (Condition)obj;
      boolean res = operator == other.operator
          && negate == other.negate
          && safeEquals(operand0, other.operand0)
          && safeEquals(operand1, other.operand1);
      return res;
    }
    return false;
  }

  @Override
  public boolean needsEval() {
    return true;
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    boolean result = negate ? !compare(env) : compare(env);
    return result ? Constants.TRUE : Constants.FALSE;
  }
  
  @Override
  public NodeType type() {
    return NodeType.CONDITION;
  }
  
  @Override
  public void repr(Buffer buf) {
    if (negate) {
      buf.append("not ");
    }
    buf.append('(');
    operand0.repr(buf);
    buf.append(' ').append(operator.toString()).append(' ');
    operand1.repr(buf);
    buf.append(')');
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(operator.toString());
    if (negate) {
      buf.append(" [negate]");
    }
    buf.append('\n').incrIndent().indent();
    operand0.modelRepr(buf);
    buf.append('\n').indent();
    operand1.modelRepr(buf);
    buf.append('\n').decrIndent();
  }
  
  private boolean compare(ExecEnv env) throws LessException {
    Node left = operand0.needsEval() ? operand0.eval(env) : operand0;
    Node right = operand1.needsEval() ? operand1.eval(env) : operand1;
    
    switch (operator) {
      case ADD:
      case DIVIDE:
      case MULTIPLY:
      case SUBTRACT:
        throw new LessException(ExecuteErrorMaker.expectedBoolOp(operator));
        
      case AND:
        return conjunction(env, left, right);
        
      case OR:
        return disjunction(env, left, right);
        
      default:
        break;
    }
    
    NodeType type = left.type();
    int result = -1;
    switch (type) {
      case ANONYMOUS:
        result = compare(env, (Anonymous)left, right);
        break;
        
      case COLOR:
        result = compare((BaseColor)left, right);
        break;
        
      case DIMENSION:
        result = compare((Dimension)left, right);
        break;
        
      case KEYWORD:
      case TRUE:
      case FALSE:
        result = compare((Keyword)left, right);
        break;
        
      case QUOTED:
        result = compare(env, (Quoted)left, right);
        break;
        
      default:
        throw new LessException(ExecuteErrorMaker.uncomparableType(type));
    }

    switch (result) {
      case -1:
        return operator == LESS_THAN || operator == LESS_THAN_OR_EQUAL || operator == NOT_EQUAL;
      case 0:
        return operator == EQUAL || operator == LESS_THAN_OR_EQUAL || operator == GREATER_THAN_OR_EQUAL;
      case 1:
        return operator == GREATER_THAN || operator == GREATER_THAN_OR_EQUAL || operator == NOT_EQUAL; 
      default:
        throw new RuntimeException("Serious error: comparison functions must return one of [-1, 0, 1]. Got " + result);
    }
  }

  private boolean conjunction(ExecEnv env, Node left, Node right) throws LessException {
    return truthValue(env, left) && truthValue(env, right);
  }

  private boolean disjunction(ExecEnv env, Node left, Node right) throws LessException {
    return truthValue(env, left) || truthValue(env, right);
  }
  
  private int compare(ExecEnv env, Anonymous anon, Node arg) throws LessException {
    String value = env.context().render(arg);
    return anon.value().compareTo(value);
  }
  
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
  
  private int compare(BaseColor color, Node arg) throws LessException {
    if (arg.is(NodeType.KEYWORD)) {
      Keyword kwd = (Keyword)arg;
      RGBColor tmp = RGBColor.fromName(kwd.value());
      if (tmp != null) {
        arg = tmp;
      }
    }
    if (!arg.is(NodeType.COLOR)) {
      return -1;
    }
    RGBColor color0 = color.toRGB();
    RGBColor color1 = ((BaseColor)arg).toRGB();
    return color0.red() == color1.red()
        && color0.green() == color1.green()
        && color0.blue() == color1.blue()
        && color0.alpha() == color1.alpha() ? 0 : -1;
  }
  
  private int compare(Dimension dim0, Node arg) throws LessException {
    if (arg instanceof Dimension) {
      Dimension dim1 = (Dimension)arg;
      double value0 = dim0.value();
      Unit unit0 = dim0.unit();
      Unit unit1 = dim1.unit();
      unit0 = (unit0 == Unit.PERCENTAGE ? null : unit0);
      unit1 = (unit1 == Unit.PERCENTAGE ? null : unit1);

      double factor = 1.0;
      double scaled = dim1.value();
      if (dim0.unit() != dim1.unit()) { 
        factor = UnitConversions.factor(unit1, unit0);
        if (factor == 0.0) {
          return -1;
        }
        scaled *= factor;
      }
      return value0 < scaled ? -1 : (value0 > scaled ? 1 : 0);
    }
    return -1;
  }
  
  private int compare(Keyword keyword, Node arg) throws LessException {
    if (arg instanceof Keyword) {
      return compareTo(keyword.value(), ((Keyword)arg).value());
    }
    return -1;
  }
  
  private int compare(ExecEnv env, Quoted quoted, Node arg) throws LessException {
    return compareTo(render(env, quoted), render(env, arg));
  }
  
  private String render(ExecEnv env, Node arg) throws LessException {
    switch (arg.type()) {
      case ANONYMOUS:
        return ((Anonymous)arg).value();
        
      case KEYWORD:
        return ((Keyword)arg).value();

      default:
        return env.context().render(arg);
    }
  }
  
  private int compareTo(String left, String right) {
    int res = left.compareTo(right);
    return res < 0 ? -1 : (res > 0) ? 1 : 0;
  }
  
}
