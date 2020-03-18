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

import static com.squarespace.less.exec.Function.rgb;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;


/**
 * Color channel function implementations.
 *
 * http://lesscss.org/functions/#color-channel
 */
public class ColorChannelFunctions implements Registry<Function> {

  @Override
  public void registerPlugins(SymbolTable<Function> table) {
    table.add(ALPHA);
    table.add(BLUE);
    table.add(GREEN);
    table.add(HUE);
    table.add(LIGHTNESS);
    table.add(LUMA);
    table.add(LUMINANCE);
    table.add(RED);
    table.add(SATURATION);
  }

  public static final Function ALPHA = new Function("alpha", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.alpha());
    }
  };

  public static final Function BLUE = new Function("blue", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.blue());
    }
  };

  public static final Function GREEN = new Function("green", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.green());
    }
  };

  public static final Function HUE = new Function("hue", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(hsl(args.get(0)).hue());
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
      return legacyLuma(args);
    }
  };

  public static final Function LUMINANCE = new Function("luminance", "c") {
    @Override
    public Node invoke(ExecEnv env, java.util.List<Node> args) throws LessException {
      return legacyLuma(args);
    }
  };

  private static Node legacyLuma(List<Node> args) throws LessException {
    return new Dimension(Math.round(rgb(args.get(0)).luma() * 100.0), Unit.PERCENTAGE);
  }

  public static final Function RED = new Function("red", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.red());
    }
  };

  public static final Function SATURATION = new Function("saturation", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).saturation() * 100.0), Unit.PERCENTAGE);
    }
  };

}
