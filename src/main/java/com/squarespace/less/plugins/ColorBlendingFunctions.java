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
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;


/**
 * Color blending function implementations.
 *
 * http://lesscss.org/functions/#color-blending
 */
public class ColorBlendingFunctions implements Registry<Function> {

  public static final Function AVERAGE = new Function("average", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = (c1.red() + c2.red()) / 2.0;
      double green = (c1.green() + c2.green()) / 2.0;
      double blue = (c1.blue() + c2.blue()) / 2.0;
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function DIFFERENCE = new Function("difference", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = Math.abs(c1.red() - c2.red());
      double green = Math.abs(c1.green() - c2.green());
      double blue = Math.abs(c1.blue() - c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function EXCLUSION = new Function("exclusion", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = exclusion(c1.red(), c2.red());
      double green = exclusion(c1.green(), c2.green());
      double blue = exclusion(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function HARDLIGHT = new Function("hardlight", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = ColorBlendingFunctions.hardlight(c1.red(), c2.red());
      double green = ColorBlendingFunctions.hardlight(c1.green(), c2.green());
      double blue = ColorBlendingFunctions.hardlight(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function MULTIPLY = new Function("multiply", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = (c1.red() * c2.red() / 255.0);
      double green = (c1.green() * c2.green() / 255.0);
      double blue = (c1.blue() * c2.blue() / 255.0);
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function NEGATION = new Function("negation", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = negation(c1.red(), c2.red());
      double green = negation(c1.green(), c2.green());
      double blue = negation(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function OVERLAY = new Function("overlay", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = overlay(c1.red(), c2.red());
      double green = overlay(c1.green(), c2.green());
      double blue = overlay(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function SCREEN = new Function("screen", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = screen(c1.red(), c2.red());
      double green = screen(c1.green(), c2.green());
      double blue = screen(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function SOFTLIGHT = new Function("softlight", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = ColorBlendingFunctions.softlight(c1.red(), c2.red());
      double green = ColorBlendingFunctions.softlight(c1.green(), c2.green());
      double blue = ColorBlendingFunctions.softlight(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  private static double exclusion(double c1, double c2) {
    return (c1 + c2 * (255 - c1 - c1) / 255.0);
  }

  private static double hardlight(double c1, double c2) {
    if (c2 < 128) {
      return (2 * c2 * c1 / 255.0);
    }
    return (255 - 2 * (255 - c2) * (255 - c1) / 255.0);
  }

  private static double negation(double c1, double c2) {
    return 255 - Math.abs(255 - c2 - c1);
  }

  private static double overlay(double c1, double c2) {
    if (c1 < 128) {
      return 2 * c1 * c2 / 255.0;
    }
    return 255 - 2 * (255 - c1) * (255 - c2) / 255.0;
  }

  private static double softlight(double c1, double c2) {
    double t = c2 * c1 / 255.0;
    double r = t + c1 * (255.0 - (255.0 - c1) * (255.0 - c2) / 255.0 - t) / 255.0;
    return r;
  }

  private static double screen(double c1, double c2) {
    return 255 - (255 - c1) * (255 - c2) / 255.0;
  }

}
