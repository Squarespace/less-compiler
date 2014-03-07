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

import static com.squarespace.less.core.ExecuteErrorMaker.incompatibleUnits;
import static com.squarespace.less.core.ExecuteErrorMaker.invalidOperation;
import static com.squarespace.less.model.NodeType.COLOR;

import com.squarespace.less.ErrorInfo;
import com.squarespace.less.LessException;
import com.squarespace.less.Options;
import com.squarespace.less.exec.ExecEnv;


public abstract class BaseColor extends BaseNode {

  public abstract Colorspace getColorspace();

  public abstract RGBColor toRGB();

  public abstract HSLColor toHSL();

  public static RGBColor fromHex(String raw) {
    int[] c = Colors.hexToRGB(raw);
    return new RGBColor(c[0], c[1], c[2]);
  }

  public static RGBColor fromDimension(Dimension dim) {
    int val = (int)dim.value();
    return new RGBColor(val, val, val);
  }

  public static RGBColor fromName(String name) {
    int[] c = Colors.nameToRGB(name);
    if (c != null) {
      return new RGBColor(c[0], c[1], c[2], true);
    }
    return null;
  }

  public static double clamp(double val, double min, double max) {
    return Math.min(Math.max(val, min), max);
  }

  @Override
  public NodeType type() {
    return COLOR;
  }

  @Override
  public Node operate(ExecEnv env, Operator op, Node arg) throws LessException {
    Options opts = env.context().options();
    if (arg.is(NodeType.COLOR)) {
      return operate(op, this, (BaseColor)arg);

    } else if (arg.is(NodeType.DIMENSION)) {
      // Dimensions that have units cannot be added/multiplied with a color.
      Dimension dim = (Dimension)arg;
      if (dim.unit() != null) {
        ErrorInfo info = incompatibleUnits(dim.unit(), NodeType.COLOR);
        if (opts.strict()) {
          throw new LessException(info);
        }
        if (!opts.hideWarnings()) {
          env.addWarning(info.getMessage() + ".. stripping unit.");
        }
      }
      return operate(op, this, fromDimension((Dimension)arg));

    } else {
      ErrorInfo info = invalidOperation(op, type());
      if (opts.strict()) {
        throw new LessException(info);
      }
      if (!opts.hideWarnings()) {
        env.addWarning(info.getMessage() + ".. ignoring the right-hand operand.");
      }
      return this;
    }
  }

  private BaseColor operate(Operator op, BaseColor arg0, BaseColor arg1) throws LessException {
    RGBColor c0 = arg0.toRGB();
    RGBColor c1 = arg1.toRGB();
    int r = c0.red();
    int g = c0.green();
    int b = c0.blue();
    double a = c0.alpha() + c1.alpha(); // per less.js
    switch (op) {
      case ADD:
        return new RGBColor(r + c1.red(), g + c1.green(), b + c1.blue(), a);

      case DIVIDE:
        return new RGBColor(r / c1.red(), g / c1.green(), b / c1.blue(), a);

      case MULTIPLY:
        return new RGBColor(r * c1.red(), g * c1.green(), b * c1.blue(), a);

      case SUBTRACT:
        return new RGBColor(r - c1.red(), g - c1.green(), b - c1.blue(), a);

      default:
        throw new LessException(invalidOperation(op, NodeType.COLOR));
    }
  }

}
