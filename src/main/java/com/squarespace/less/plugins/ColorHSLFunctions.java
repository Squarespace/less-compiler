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
import com.squarespace.less.model.HSLColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;


public class ColorHSLFunctions implements Registry<Function> {

  public static final Function HSL = new Function("hsl", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double lightness = percent(args.get(2));
      return new HSLColor(hue % 360 / 360.0, saturation, lightness, 1.0);
    }
  };

  public static final Function HSLA = new Function("hsla", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double lightness = percent(args.get(2));
      double alpha = percent(args.get(3));
      return new HSLColor(hue % 360 / 360.0, saturation, lightness, alpha);
    }
  };

  public static final Function HSV = new Function("hsv", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double value = percent(args.get(2));
      return RGBColor.fromHSVA(hue % 360 / 360.0, saturation, value, 1.0);
    }
  };

  public static final Function HSVA = new Function("hsva", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double value = percent(args.get(2));
      double alpha = percent(args.get(3));
      return RGBColor.fromHSVA(hue % 360 / 360.0, saturation, value, alpha);
    }
  };

  public static final Function HUE = new Function("hue", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(hsl(args.get(0)).hue());
    }
  };

  public static final Function SATURATION = new Function("saturation", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).saturation() * 100.0), Unit.PERCENTAGE);
    }
  };

  public static final Function LIGHTNESS = new Function("lightness", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).lightness() * 100.0), Unit.PERCENTAGE);
    }
  };

  public static final Function LUMA = new Function("luma", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(rgb(args.get(0)).luma() * 100.0), Unit.PERCENTAGE);
    }
  };

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
