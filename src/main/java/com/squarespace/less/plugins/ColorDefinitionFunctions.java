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
import com.squarespace.less.model.HSLColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;


/**
 * Color definition function implementations.
 *
 * http://lesscss.org/functions/#color-definition
 */
public class ColorDefinitionFunctions implements Registry<Function> {

  public static final Function RGB = new Function("rgb", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double red = scaled(args.get(0), 256);
      double green = scaled(args.get(1), 256);
      double blue = scaled(args.get(2), 256);
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function RGBA = new Function("rgba", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double red = scaled(args.get(0), 256);
      double green = scaled(args.get(1), 256);
      double blue = scaled(args.get(2), 256);
      double alpha = percent(args.get(3));
      return new RGBColor(red, green, blue, alpha);
    }
  };

  public static final Function ARGB = new Function("argb", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return color.toARGB();
    }
  };

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

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
