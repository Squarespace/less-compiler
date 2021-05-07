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

import java.util.HashMap;
import java.util.Map;


/**
 * Represents the unit in a {@link Dimension}.
 */
public enum Unit {

  PERCENTAGE ("%", "percentage"),

  // ABSOLUTE LENGTHS

  // centimeters
  CM ("cm", "centimeters"),
  // millimeters
  MM ("mm", "millimeters"),
  // inches (1in == 2.54cm)
  IN ("in", "inches"),
  // pixels (1px == 1/96in)
  PX ("px", "pixels"),
  // points (1pt == 1/72in)
  PT ("pt", "points"),
  // picas  (1pc == 12pt)
  PC ("pc", "picas"),

  // FONT-RELATIVE LENGTHS

  // width of the '0' (ZERO U+0030) glyph in the element's font
  CH ("ch", "advance measure of '0' glyph"),
  // font size of element
  EM ("em", "element font size"),
  // x-height of the element's font
  EX ("ex", "x-height of element's font"),
  // font size of the root element
  REM ("rem", "font size of root element"),

  // VIEWPORT-RELATIVE LENGTHS

  // 1% of viewport's height
  VH ("vh", "viewport's height"),
  // 1% of viewport's width
  VW ("vw", "viewport's width"),
  // 1% of viewport's smaller dimension
  VMIN ("vmin", "viewport's smaller dimension"),
  // 1% of viewport's larger dimension
  VMAX ("vmax", "viewport's larger dimension"),
  // [bug / typo in less 1.3.3 'vm']
  VM ("vm", ""),

  // GRID

  FR ("fr", "fractions"),

  // TIME

  // seconds
  S ("s", "seconds"),
  // milliseconds
  MS ("ms", "milliseconds"),

  // RESOLUTIONS

  // dots per inch
  DPI ("dpi", "dots per inch"),
  // dots per centimeter
  DPCM ("dpcm", "dots per centimeter"),
  // dots per 'px' unit (1dppx == 96dpi)
  DPPX ("dppx", "dots per 'px' unit"),

  // FREQUENCIES

  // Hertz
  HZ ("hz", "hertz"),
  // KiloHertz (1khz == 1000hz)
  KHZ ("khz", "kilohertz"),

  // ANGLES

  // Degrees
  DEG ("deg", "degrees"),
  // Gradians
  GRAD ("grad", "gradians"),
  // Radians
  RAD ("rad", "radians"),
  // Turns
  TURN ("turn", "turns");

  /**
   * Regular expression to match a unit identifier.
   *
   * NOTE: Keep this sorted roughly by most-frequently-used first
   * NOTE: 'vm' is not a real unit, but is added here since less.js references it
   */
  public static final String REGEX =
      "px|%|em|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn|fr|dpi|dpcm|dppx|rem|vw|vh|vmin|vmax|ch|hz|khz|vm";

  /**
   * Mapping from a unit's representation to its value.
   */
  private static final Map<String, Unit> UNIT_MAP = new HashMap<>();

  // Build up the mapping from a unit's representation to its enumerated value.
  static {
    Unit[] values = Unit.values();
    for (int i = 0; i < values.length; i++) {
      Unit unit = values[i];
      UNIT_MAP.put(unit.repr(), unit);
    }
  }

  /**
   * Representation of the unit, e.g. "px".
   */
  private final String repr;

  /**
   * Human-readable representation for the unit, e.g. "pixels".
   */
  private final String humanRepr;

  /**
   * Construct a unit with the given representations.
   */
  Unit(String repr, String humanRepr) {
    this.repr = repr;
    this.humanRepr = humanRepr;
  }

  /**
   * Return the shorthand representation.
   */
  public String repr() {
    return repr;
  }

  /**
   * Return the human-readable representation.
   */
  public String humanRepr() {
    return humanRepr;
  }

  /**
   * Get the unit for a given raw string.
   */
  public static Unit get(String raw) {
    return UNIT_MAP.get(raw.toLowerCase());
  }

  /**
   * TODO: Format a double to a unit-specific level of precision.
   */
  public String format(double val) {
    // XXX: precision
    return "";
  }

  @Override
  public String toString() {
    return repr.toUpperCase() + (humanRepr.isEmpty() ? "" : " (" + humanRepr + ")");
  }
}
