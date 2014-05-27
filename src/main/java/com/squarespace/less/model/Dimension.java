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

import static com.squarespace.less.core.ExecuteErrorMaker.expectedMathOp;
import static com.squarespace.less.core.ExecuteErrorMaker.incompatibleUnits;
import static com.squarespace.less.core.ExecuteErrorMaker.invalidOperation;

import com.squarespace.less.LessErrorInfo;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.exec.ExecEnv;


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
  public int hashCode() {
    return super.hashCode();
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

  @Override
  public Node operate(ExecEnv env, Operator op, Node node) throws LessException {
    if (!node.is(NodeType.DIMENSION)) {
      throw new LessException(invalidOperation(op, type()));
    }

    LessOptions opts = env.context().options();
    Dimension dim = (Dimension)node;
    Unit newUnit = (unit != null) ? unit : dim.unit;
    double result = 0.0;

    double factor = UnitConversions.factor(dim.unit, unit);
    double scaled = dim.value;
    if (factor == 0.0) {
      if (dim.unit != Unit.PERCENTAGE) {
        // Emit a warning if we're converting between incompatible units
        LessErrorInfo info = incompatibleUnits(unit, dim.unit);
        if (!opts.hideWarnings()) {
          env.addWarning(info.getMessage() + ".. stripping unit.");
        }
      }
      factor = 1.0;
    }
    scaled = dim.value * factor;

    switch (op) {

      case ADD:
        result = value + scaled;
        break;

      case DIVIDE:
        if (scaled == 0.0) {
          LessErrorInfo info = ExecuteErrorMaker.divideByZero(this);
          if (opts.strict()) {
            throw new LessException(info);
          } else if (!opts.hideWarnings()) {
            env.addWarning(info.getMessage() + "..  using " + this.repr());
          }
        } else {
          result = value / scaled;
        }
        break;

      case MULTIPLY:
        result = value * scaled;
        break;

      case SUBTRACT:
          result = value - scaled;
        break;

      default:
        throw new LessException(expectedMathOp(op));
    }

    return new Dimension(result, newUnit);
  }

  /**
   * All math operations between Dimension instances happen here.
   *
   * TODO: this has more advanced handling of percentages. preserving for future reference. - phensley
   */
//  public Node new_operate(ExecEnv env, Operator op, Node node) throws LessException {
//    if (!node.is(NodeType.DIMENSION)) {
//      throw new LessException(invalidOperation(op, type()));
//    }
//
//    Options opts = env.context().options();
//    Dimension dim = (Dimension)node;
//    Unit new_unit = unit;
//    double result = 0.0;
//
//    // Special case when the left-hand is a percentage.
//    if (unit == PERCENTAGE) {
//      if (dim.unit != PERCENTAGE && dim.unit != null && op != Operator.MULTIPLY) {
//        ErrorInfo info = percentMathOrder(dim);
//        if (opts.strict()) {
//          throw new LessException(info);
//        } else if (!opts.hideWarnings()) {
//          env.addWarning(info.getMessage() + ".. stripping unit.");
//        }
//        dim = new Dimension(dim.value());
//      }
//      switch (op) {
//
//        case ADD:
//          result = value + dim.value;
//          break;
//
//        case DIVIDE:
//          if (dim.unit == PERCENTAGE) {
//            result = value / (dim.value * 0.01);
//          } else {
//            result = value / dim.value;
//          }
//          break;
//
//        case MULTIPLY:
//          if (dim.unit == PERCENTAGE) {
//            result = value * (dim.value * 0.01);
//
//          } else if (dim.unit == null) {
//            result = value * dim.value;
//
//          } else {
//            result = (value * 0.01) * dim.value;
//            new_unit = dim.unit;
//          }
//          break;
//
//        case SUBTRACT:
//          result = value - dim.value;
//          break;
//
//        default:
//          throw new LessException(expectedMathOp(op));
//      }
//      return new Dimension(result, new_unit);
//    }
//
//    if (dim.unit != Unit.PERCENTAGE) {
//      new_unit = (unit != null) ? unit : dim.unit;
//    }
//    double factor = UnitConversions.factor(dim.unit, unit);
//    if (factor == 0.0 && dim.unit != Unit.PERCENTAGE) {
//      ErrorInfo info = incompatibleUnits(unit, dim.unit);
//      if (opts.strict()) {
//        throw new LessException(info);
//      } else if (!opts.hideWarnings()) {
//        env.addWarning(info.getMessage() + ".. stripping unit.");
//      }
//      factor = 1.0;
//      dim = new Dimension(dim.value());
//    }
//    double scaled = dim.value * factor;
//    switch (op) {
//
//      case ADD:
//        if (dim.unit == PERCENTAGE) {
//          result = value + (value * dim.value * 0.01);
//        } else {
//          result = value + scaled;
//        }
//        break;
//
//      case DIVIDE:
//        if (dim.unit == PERCENTAGE) {
//          result = value / (dim.value * 0.01);
//        } else {
//          result = value / scaled;
//        }
//        break;
//
//      case MULTIPLY:
//        if (dim.unit == PERCENTAGE) {
//          result = value * (dim.value * 0.01);
//        } else {
//          result = value * scaled;
//        }
//        break;
//
//      case SUBTRACT:
//        if (dim.unit == PERCENTAGE) {
//          result = value - (value * dim.value * 0.01);
//        } else {
//          result = value - scaled;
//        }
//        break;
//
//      default:
//        throw new LessException(expectedMathOp(op));
//    }
//
//    return new Dimension(result, new_unit);
//  }

}

