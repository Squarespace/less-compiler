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

  /**
   * Name of the function. This gets added to a symbol table so the function
   * implementation can be resolved at execution time.
   */
  protected final String name;

  /**
   * Specification of the arguments this function accepts.
   */
  protected final ArgSpec spec;

  /**
   * Construct a function named {@code name} with the raw argument specification.
   */
  public Function(String name, String spec) {
    this.name = name;
    this.spec = ArgSpec.fromString(spec);
  }

  /**
   * Construct a function named {@code name} with the parsed argument specification.
   */
  public Function(String name, ArgSpec spec) {
    this.name = name;
    this.spec = spec;
  }

  /**
   * Returns the name of the function.
   */
  public String name() {
    return name;
  }

  /**
   * Returns the argument specification for the function.
   */
  public ArgSpec spec() {
    return spec;
  }

  /**
   * Invokes the functions with the given execution environment and arguments.
   */
  public abstract Node invoke(ExecEnv env, List<Node> args) throws LessException;

  /**
   * Converts a {@link Dimension} value to a percentage.
   */
  public static double percent(Node node) throws LessException {
    Dimension dim = (Dimension)node;
    return Unit.PERCENTAGE.equals(dim.unit()) ? dim.value() * 0.01 : dim.value();
  }

  /**
   * Returns the value from a {@link Dimension} argument.
   */
  public static double number(Node node) throws LessException {
    return ((Dimension)node).value();
  }

  /**
   * Returns a {@link Dimension} value scaled by the given amount.
   */
  public static double scaled(Node node, double scale) throws LessException {
    Dimension dim = (Dimension)node;
    double value = number(node);
    return Unit.PERCENTAGE.equals(dim.unit()) ? (value * .01) * scale : value;
  }

  /**
   * Casts the argument to an HSL color.
   */
  public static HSLColor hsl(Node node) throws LessException {
    return ((BaseColor)node).toHSL();
  }

  /**
   * Casts the argument to an RGB color.
   */
  public static RGBColor rgb(Node node) throws LessException {
    return ((BaseColor)node).toRGB();
  }

}
