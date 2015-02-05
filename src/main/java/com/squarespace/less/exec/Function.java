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

package com.squarespace.less.exec;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.HSLColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Unit;


/**
 * Base class for plug-in function implementations. Provides some methods to
 * assist in parsing arguments.
 */
public abstract class Function {

  protected final String name;

  protected final ArgSpec spec;

  public Function(String name, String spec) {
    this.name = name;
    this.spec = ArgSpec.fromString(spec);
  }

  public Function(String name, ArgSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  public String name() {
    return name;
  }

  public ArgSpec spec() {
    return spec;
  }

  public abstract Node invoke(ExecEnv env, List<Node> args) throws LessException;

  public static double percent(Node node) throws LessException {
    Dimension dim = (Dimension)node;
    return (dim.unit() == Unit.PERCENTAGE) ? dim.value() * 0.01 : dim.value();
  }

  public static double number(Node node) throws LessException {
    return ((Dimension)node).value();
  }

  public static double scaled(Node node, double scale) throws LessException {
    Dimension dim = (Dimension)node;
    double value = number(node);
    return dim.unit() == Unit.PERCENTAGE ? (value * .01) * scale : value;
  }

  public static HSLColor hsl(Node node) throws LessException {
    return ((BaseColor)node).toHSL();
  }

  public static RGBColor rgb(Node node) throws LessException {
    return ((BaseColor)node).toRGB();
  }

}
