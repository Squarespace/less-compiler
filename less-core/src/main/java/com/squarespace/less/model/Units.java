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

import static com.squarespace.less.model.Unit.define;

public class Units {

  /** percentage */
  public static final Unit PERCENTAGE = define("%", "percentage");


  // ABSOLUTE LENGTHS

  /** centimeters */
  public static final Unit CM = define("cm", "centimeters");

  /** millimeters */
  public static final Unit MM = define("mm", "millimeters");

  /** inches (1in == 2.54cm) */
  public static final Unit IN = define("in", "inches");

  /** pixels (1px == 1/96in) */
  public static final Unit PX = define("px", "pixels");

  /** points (1pt == 1/72in) */
  public static final Unit PT = define("pt", "points");

  /** picas  (1pc == 12pt) */
  public static final Unit PC = define("pc", "picas");


  // FONT-RELATIVE LENGTHS

  /** width of the '0' (ZERO U+0030) glyph in the element's font */
  public static final Unit CH = define("ch", "advance measure of '0' glyph");

  /** font size of element */
  public static final Unit EM = define("em", "element font size");

  /** x-height of the element's font */
  public static final Unit EX = define("ex", "x-height of element's font");

  /** font size of the root element */
  public static final Unit REM = define("rem", "font size of root element");


  // VIEWPORT-RELATIVE LENGTHS

  /** 1% of viewport's height */
  public static final Unit VH = define("vh", "viewport's height");

  /** 1% of viewport's width */
  public static final Unit VW = define("vw", "viewport's width");

  /** 1% of viewport's smaller dimension */
  public static final Unit VMIN = define("vmin", "viewport's smaller dimension");

  /** 1% of viewport's larger dimension */
  public static final Unit VMAX = define("vmax", "viewport's larger dimension");


  // TIME

  /** seconds */
  public static final Unit S = define("s", "seconds");

  /** milliseconds */
  public static final Unit MS = define("ms", "milliseconds");


  // RESOLUTIONS

  /** dots per inch */
  public static final Unit DPI = define("dpi", "dots per inch");

  /** dots per centimeter */
  public static final Unit DPCM = define("dpcm", "dots per centimeter");

  /** dots per 'px' unit (1dppx == 96dpi) */
  public static final Unit DPPX = define("dppx", "dots per 'px' unit");


  // FREQUENCIES

  /** Hertz */
  public static final Unit HZ = define("hz", "hertz");

  /** KiloHertz (1khz == 1000hz) */
  public static final Unit KHZ = define("khz", "kilohertz");


  // ANGLES

  /** Degrees */
  public static final Unit DEG = define("deg", "degrees");

  /** Gradians */
  public static final Unit GRAD = define("grad", "gradians");

  /** Radians */
  public static final Unit RAD = define("rad", "radians");

  /** Turns */
  public static final Unit TURN = define("turn", "turns");

  private Units() {
  }

}
