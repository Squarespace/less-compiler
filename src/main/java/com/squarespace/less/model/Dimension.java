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
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a numeric value with optional units. Note that since Javascript
 * uses a 64-bit signed in Number operations, we can get away with using
 * double for all LESS math operations.  The operations involved should never
 * require the full scale and precision of a double.
 */
public class Dimension extends BaseNode {

  /**
   * Numeric value of the dimension.
   */
  protected final double value;

  /**
   * Optional unit (null == no unit)
   */
  protected final Unit unit;

  /**
   * Construct a dimension with the given value and no unit specified.
   */
  public Dimension(double value) {
    this(value, null);
  }

  /**
   * Construct a dimension with the given value and unit.
   */
  public Dimension(double value, Unit unit) {
    this.value = value;
    this.unit = unit;
  }

  /**
   * Return the value for this dimension.
   */
  public double value() {
    return value;
  }

  /**
   * Return the unit for this dimension.
   */
  public Unit unit() {
    return unit;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Dimension) {
      Dimension other = (Dimension)obj;
      boolean unitEqual = LessUtils.safeEquals(unit, other.unit);
      if (Double.isNaN(value) && Double.isNaN(other.value)) {
        return unitEqual;
      } else if (Double.isInfinite(value) && Double.isInfinite(other.value)) {
        return unitEqual;
      }
      return unitEqual && value == other.value;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.DIMENSION;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    formatDouble(buf, value);
    if (unit != null) {
      buf.append(unit.repr());
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(value);
    if (unit != null) {
      buf.append(' ').append(unit.toString());
    }
  }

  /**
   * Apply an operation where the current instance is the left operand,
   * and the {@code node} argument is the right.
   */
  @Override
  public Node operate(ExecEnv env, Operator op, Node node) throws LessException {
    if (!(node instanceof Dimension)) {
      throw new LessException(invalidOperation(op, type(), node.type()));
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

}

