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

package com.squarespace.less.plugins;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.UnitConversions;


/**
 * Math function implementations.
 *
 * http://lesscss.org/functions/#math-functions
 */
public class MathFunctions implements Registry<Function> {

  @Override
  public void registerPlugins(SymbolTable<Function> table) {
    table.add(ABS);
    table.add(ACOS);
    table.add(ASIN);
    table.add(ATAN);
    table.add(CEIL);
    table.add(COS);
    table.add(FLOOR);
    table.add(MAX);
    table.add(MIN);
    table.add(MOD);
    table.add(PERCENTAGE);
    table.add(PI);
    table.add(POW);
    table.add(ROUND);
    table.add(SIN);
    table.add(SQRT);
    table.add(TAN);
  }

  public static final Function ABS = new Function("abs", "d") {
    public Node invoke(ExecEnv env, java.util.List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.abs(dim.value()), dim.unit());
    };
  };

  public static final Function ASIN = new Function("asin", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double value = Math.asin(((Dimension)args.get(0)).value());
      return new Dimension(value, Unit.RAD);
    }
  };

  public static final Function ACOS = new Function("acos", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double value = Math.acos(((Dimension)args.get(0)).value());
      return new Dimension(value, Unit.RAD);
    }
  };

  public static final Function ATAN = new Function("atan", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double value = Math.atan(((Dimension)args.get(0)).value());
      return new Dimension(value, Unit.RAD);
    }
  };

  public static final Function CEIL = new Function("ceil", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.ceil(dim.value()), dim.unit());
    }
  };

  public static final Function COS = new Function("cos", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return trigResult(TrigFunction.COS, args.get(0));
    }
  };

  public static final Function FLOOR = new Function("floor", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.floor(dim.value()), dim.unit());
    }
  };

  public static final Function MAX = new Function("max", "*.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension result = calculateMinOrMax(args, false);
      return (result == null) ? null : result;
    }
  };

  public static final Function MIN = new Function("min", "*.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node result = calculateMinOrMax(args, true);
      return (result == null) ? null : result;
    }
  };

  public static final Function MOD = new Function("mod", "dd") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dividend = (Dimension)args.get(0);
      double divisor = ((Dimension)args.get(1)).value();
      double result = Double.NaN;
      if (divisor != 0.0) {
        result = dividend.value() % divisor;
      }
      return new Dimension(result, dividend.unit());
    }
  };

  public static final Function PERCENTAGE = new Function("percentage", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(dim.value() * 100, Unit.PERCENTAGE);
    }
  };

  public static final Function PI = new Function("pi", "") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.PI);
    }
  };

  public static final Function POW = new Function("pow", "dd") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension base = (Dimension)args.get(0);
      Dimension exp = (Dimension)args.get(1);
      double value = Math.pow(base.value(), exp.value());
      return new Dimension(value, base.unit());
    }
  };

  public static final Function ROUND = new Function("round", "d:n") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double places = 0.0;
      int size = args.size();
      Dimension dim = (Dimension)args.get(0);
      if (size == 2) {
        places = Math.max(((Dimension)args.get(1)).value(), 0);
      }
      double scale = Math.pow(10, places);
      return new Dimension(Math.round(dim.value() * scale) / scale, dim.unit());
    }
  };

  public static final Function SIN = new Function("sin", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return trigResult(TrigFunction.SIN, args.get(0));
    }
  };

  public static final Function SQRT = new Function("sqrt", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.sqrt(dim.value()), dim.unit());
    }
  };

  public static final Function TAN = new Function("tan", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return trigResult(TrigFunction.TAN, args.get(0));
    }
  };

  private static Dimension calculateMinOrMax(List<Node> args, boolean minimum) throws LessException {
    // These will be set to value of first argument.
    double value = 0.0;
    Unit unit = null;

    int size = args.size();
    for (int i = 0; i < size; i++) {
      Node node = args.get(i);
      if (!(node instanceof Dimension)) {
        // Non-dimension is invalid. Bail out and return static representation of function call.
        return null;
      }

      Dimension dim = (Dimension)node;
      if (i == 0) {
        value = dim.value();
        unit = dim.unit();

      } else if (dim.unit() != unit) {
        // Mixed units are invalid. Bail out and return static representation of function call
        return null;

      } else {
        value = (minimum) ? Math.min(value, dim.value()) : Math.max(value, dim.value());
      }

    }
    return new Dimension(value, unit);
  }

  private enum TrigFunction {
    SIN,
    COS,
    TAN
  }

  private static Node trigResult(TrigFunction function, Node argument) {
    Dimension dim = (Dimension)argument;
    double factor = UnitConversions.factor(dim.unit(), Unit.RAD);
    double result = dim.value() * factor;
    switch (function) {
      case SIN:
        result = Math.sin(result);
        break;

      case COS:
        result = Math.cos(result);
        break;

      case TAN:
        result = Math.tan(result);
        break;

      // Checkstyle-enforced, reducing our code coverage percentage
      default:
        throw new RuntimeException("unsupported enum value " + function);
    }
    return new Dimension(result);
  }

}
