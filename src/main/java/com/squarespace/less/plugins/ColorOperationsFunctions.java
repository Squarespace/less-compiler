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
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Colors;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.HSLColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;


/**
 * Color operations function implementations.
 *
 * http://lesscss.org/functions/#color-operations
 */
public class ColorOperationsFunctions implements Registry<Function> {

  @Override
  public void registerPlugins(SymbolTable<Function> table) {
    table.add(CONTRAST);
    table.add(DARKEN);
    table.add(DESATURATE);
    table.add(FADE);
    table.add(FADEIN);
    table.add(FADEOUT);
    table.add(GREYSCALE);
    table.add(LIGHTEN);
    table.add(MIX);
    table.add(SATURATE);
    table.add(SHADE);
    table.add(SPIN);
    table.add(TINT);
  }

  public static final Function CONTRAST = new Function("contrast", "*:ccp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg = args.get(0);
      if (!(arg instanceof BaseColor)) {
        return null;
      }
      RGBColor color = rgb(arg);
      int size = args.size();
      RGBColor dark = size >= 2 ? rgb(args.get(1)) : Colors.BLACK;
      RGBColor light = size >= 3 ? rgb(args.get(2)) : Colors.WHITE;
      double threshold = size >= 4 ? number(args.get(3)) : 0.43;
      double value = (0.2126 * (color.red() / 255.0)
          + 0.7152 * (color.green() / 255.0)
          + 0.0722 * (color.blue() / 255.0)) * color.alpha();
      if (value < threshold) {
        return light;
      }
      return dark;
    }
  };

  public static final Function DARKEN = new Function("darken", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation(), hsl.lightness() - value, hsl.alpha());
    }
  };

  public static final Function DESATURATE = new Function("desaturate", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation() - value, hsl.lightness(), hsl.alpha());
    }
  };

  public static final Function FADE = new Function("fade", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor rgb = rgb(args.get(0));
      double alpha = number(args.get(1)) * 0.01;
      return new RGBColor(rgb.red(), rgb.green(), rgb.blue(), alpha);
    }
  };

  public static final Function FADEIN = new Function("fadein", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor rgb = rgb(args.get(0));
      double amount = number(args.get(1)) * 0.01;
      return new RGBColor(rgb.red(), rgb.green(), rgb.blue(), rgb.alpha() + amount);
    }
  };

  public static final Function FADEOUT = new Function("fadeout", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor rgb = rgb(args.get(0));
      double amount = number(args.get(1)) * 0.01;
      return new RGBColor(rgb.red(), rgb.green(), rgb.blue(), rgb.alpha() - amount);
    }
  };

  public static final Function GREYSCALE = new Function("greyscale", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      return new HSLColor(hsl.hue() / 360.0, 0, hsl.lightness(), hsl.alpha());
    }
  };

  public static final Function LIGHTEN = new Function("lighten", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation(), hsl.lightness() + value, hsl.alpha());
    }
  };

  public static final Function MIX = new Function("mix", "cc:d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double weight = 0.5;
      if (args.size() == 3) {
        weight = number(args.get(2)) / 100.0;
      }
      return ColorOperationsFunctions.mix(c1, c2, weight);
    }
  };

  public static final Function SATURATE = new Function("saturate", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      double value = number(args.get(1)) * 0.01;
      return new HSLColor(hsl.hue() / 360.0, hsl.saturation() + value, hsl.lightness(), hsl.alpha());
    }
  };

  // Deprecated from upstream
  public static final Function SHADE = new Function("shade", "cd") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      Dimension dim = (Dimension)args.get(1);
      return mix(Colors.BLACK, c1, dim.value() / 100.0);
    }
  };

  public static final Function SPIN = new Function("spin", "cp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      HSLColor hsl = hsl(args.get(0));
      Dimension amount = (Dimension)args.get(1);
      double value = amount.value();
      if (amount.unit() == Unit.PERCENTAGE) {
        value = (value / 100.0) * 360;
      }
      double hue = (hsl.hue() + value) % 360;
      hue = hue < 0 ? 360 + hue : hue;
      Node result = new HSLColor(hue / 360.0, hsl.saturation(), hsl.lightness(), hsl.alpha());
      return result;
    }
  };

  // Deprecated from upstream
  public static final Function TINT = new Function("tint", "cd") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      Dimension dim = (Dimension)args.get(1);
      double weight = dim.value() / 100.0;
      return mix(Colors.WHITE, c1, weight);
    }
  };

  private static RGBColor mix(RGBColor c1, RGBColor c2, double weight) {
    double p = weight;
    double w = p * 2 - 1;
    double a = c1.toHSL().alpha() - c2.toHSL().alpha();
    double w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0;
    double w2 = 1 - w1;
    double red = c1.red() * w1 + c2.red() * w2;
    double green = c1.green() * w1 + c2.green() * w2;
    double blue = c1.blue() * w1 + c2.blue() * w2;
    double alpha = c1.alpha() * p + c2.alpha() * (1 - p);
    return new RGBColor(red, green, blue, alpha);
  }

}

