package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.ExecuteErrorType.INVALID_OPERATION1;
import static com.squarespace.v6.template.less.ExecuteErrorType.PERCENT_MATH_ORDER;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.model.Unit.PERCENTAGE;

import com.squarespace.v6.template.less.ExecuteErrorType;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.ErrorUtils;


/**
 * Represents a numeric value with optional units. Note that since Javascript
 * uses a 64-bit signed in Number operations, we can get away with using
 * double for all LESS math operations.  The operations involved should never
 * require the full scale and precision of a double.
 */
public class Dimension extends BaseNode {

  private final double value;
  
  private final Unit unit;
  
  public Dimension(double value) {
    this(value, null);
  }
  
  public Dimension(double value, Unit unit) {
    this.value = value;
    this.unit = unit;
  }
 
  public double value() {
    return value;
  }
  
  public Unit unit() {
    return unit;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Dimension) {
      Dimension other = (Dimension)obj;
      return value == other.value && unit == other.unit;
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.DIMENSION;
  }
  
  @Override
  public void repr(Buffer buf) {
    formatDouble(buf, value);
    if (unit != null) {
      buf.append(unit.repr());
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(value);
    if (unit != null) {
      buf.append(' ').append(unit.toString());
    }
  }
  
  /**
   * All math operations between Dimension instances happen here.
   */
  @Override
  public Node operate(Operator op, Node node) throws LessException {
    if (!node.is(NodeType.DIMENSION)) {
      throw new LessException(ErrorUtils.error(INVALID_OPERATION1).type(op).arg0(type()));
    }

    Dimension dim = (Dimension)node;
    Unit new_unit = unit;
    double result = 0.0;
    
    // Special case when the left-hand is a percentage.
    if (unit == PERCENTAGE) {
      if (dim.unit != PERCENTAGE && dim.unit != null && op != Operator.MULTIPLY) {
        throw new LessException(error(PERCENT_MATH_ORDER).arg0(dim));
      }
      switch (op) {
        
        case ADD:
          result = value + dim.value;
          break;

        case DIVIDE:
          if (dim.unit == PERCENTAGE) {
            result = value / (dim.value * 0.01);
          } else {
            result = value / dim.value;
          }
          break;
          
        case MULTIPLY:
          if (dim.unit == PERCENTAGE) {
            result = value * (dim.value * 0.01);
            
          } else if (dim.unit == null) {
            result = value * dim.value;
          
          } else {
            result = (value * 0.01) * dim.value;
            new_unit = dim.unit;
          }
          break;
          
        case SUBTRACT:
          result = value - dim.value;
          break;
          
        default:
          throw new LessException(error(ExecuteErrorType.EXPECTED_MATHOP).arg0(op));
      }
      return new Dimension(result, new_unit);
    }
    
    if (dim.unit != Unit.PERCENTAGE) {
      new_unit = (unit != null) ? unit : dim.unit;
    }
    double factor = UnitConversions.factor(dim.unit, unit);
    double scaled = dim.value * factor;
    switch (op) {
      
      case ADD:
        if (dim.unit == PERCENTAGE) {
          result = value + (value * dim.value * 0.01);
        } else {
          result = value + scaled;
        }
        break;

      case DIVIDE:
        if (dim.unit == PERCENTAGE) {
          result = value / (dim.value * 0.01);
        } else {
          result = value / scaled;
        }
        break;
        
      case MULTIPLY:
        if (dim.unit == PERCENTAGE) {
          result = value * (dim.value * 0.01);
        } else {
          result = value * scaled;
        }
        break;
        
      case SUBTRACT:
        if (dim.unit == PERCENTAGE) {
          result = value - (value * dim.value * 0.01);
        } else {
          result = value - scaled;
        }
        break;
        
      default:
        throw new LessException(error(ExecuteErrorType.EXPECTED_MATHOP).arg0(op));
    }

    return new Dimension(result, new_unit);
  }
  
}

