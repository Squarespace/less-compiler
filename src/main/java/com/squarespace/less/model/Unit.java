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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents the unit in a {@link Dimension}.
 */
public class Unit {

  /**
   * Build a static unit definition.  These will be used far more often
   * than custom units, so we generate a unique id for fast comparisons.
   */
  private static Unit unit(String repr, String humanRepr) {
    Unit unit = new Unit(idSequence++, repr, humanRepr);
    UNIT_MAP.put(repr, unit);
    return unit;
  }

  /**
   * Mapping from a static unit's representation to its value.
   */
  private static final Map<String, Unit> UNIT_MAP = new HashMap<>();

  /** percentage */
  public static final Unit PERCENTAGE = unit("%", "percentage");


  // ABSOLUTE LENGTHS

  /** centimeters */
  public static final Unit CM = unit("cm", "centimeters");

  /** millimeters */
  public static final Unit MM = unit("mm", "millimeters");

  /** inches (1in == 2.54cm) */
  public static final Unit IN = unit("in", "inches");

  /** pixels (1px == 1/96in) */
  public static final Unit PX = unit("px", "pixels");

  /** points (1pt == 1/72in) */
  public static final Unit PT = unit("pt", "points");

  /** picas  (1pc == 12pt) */
  public static final Unit PC = unit("pc", "picas");


  // FONT-RELATIVE LENGTHS

  /** width of the '0' (ZERO U+0030) glyph in the element's font */
  public static final Unit CH = unit("ch", "advance measure of '0' glyph");

  /** font size of element */
  public static final Unit EM = unit("em", "element font size");

  /** x-height of the element's font */
  public static final Unit EX = unit("ex", "x-height of element's font");

  /** font size of the root element */
  public static final Unit REM = unit("rem", "font size of root element");


  // VIEWPORT-RELATIVE LENGTHS

  /** 1% of viewport's height */
  public static final Unit VH = unit("vh", "viewport's height");

  /** 1% of viewport's width */
  public static final Unit VW = unit("vw", "viewport's width");

  /** 1% of viewport's smaller dimension */
  public static final Unit VMIN = unit("vmin", "viewport's smaller dimension");

  /** 1% of viewport's larger dimension */
  public static final Unit VMAX = unit("vmax", "viewport's larger dimension");


  // TIME

  /** seconds */
  public static final Unit S = unit("s", "seconds");

  /** milliseconds */
  public static final Unit MS = unit("ms", "milliseconds");


  // RESOLUTIONS

  /** dots per inch */
  public static final Unit DPI = unit("dpi", "dots per inch");

  /** dots per centimeter */
  public static final Unit DPCM = unit("dpcm", "dots per centimeter");

  /** dots per 'px' unit (1dppx == 96dpi) */
  public static final Unit DPPX = unit("dppx", "dots per 'px' unit");


  // FREQUENCIES

  /** Hertz */
  public static final Unit HZ = unit("hz", "hertz");

  /** KiloHertz (1khz == 1000hz) */
  public static final Unit KHZ = unit("khz", "kilohertz");


  // ANGLES

  /** Degrees */
  public static final Unit DEG = unit("deg", "degrees");

  /** Gradians */
  public static final Unit GRAD = unit("grad", "gradians");

  /** Radians */
  public static final Unit RAD = unit("rad", "radians");

  /** Turns */
  public static final Unit TURN = unit("turn", "turns");

  /**
   * Increasing sequential id for fast unit comparisons. Custom unit is 0.
   */
  private static int idSequence = 1;

  /**
   * Identifier for unit.
   */
  private final int id;

  /**
   * Representation of the unit, e.g. "px".
   */
  private final String repr;

  /**
   * Human-readable representation for the unit, e.g. "pixels".
   */
  private final String humanRepr;

  /**
   * Construct a unit with the given representation.
   */
  public Unit(String repr) {
    this(repr, repr);
  }

  /**
   * Construct a unit with the given representations.
   */
  public Unit(String repr, String humanRepr) {
    this(0, repr, humanRepr);
  }

  private Unit(int id, String repr, String humanRepr) {
    this.id = id;
    this.repr = repr;
    this.humanRepr = humanRepr;
  }

  /**
   * Return the unit's internal id.
   */
  int id() {
    return id;
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
   * Return all statically-defined unit values.
   */
  public static Collection<Unit> values() {
    return UNIT_MAP.values();
  }

  /**
   * Get the unit for a given raw string.
   */
  public static Unit get(String raw) {
    String value = raw.toLowerCase();
    Unit unit = UNIT_MAP.get(value);
    return (unit == null) ? new Unit(value, value) : unit;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Unit) {
      Unit other = (Unit)obj;
      if (id != 0) {
        return other.id == 0 ? other.repr.equals(repr) : id == other.id;
      }
      return id == 0 ? repr.equals(other.repr) : id == other.id;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return repr.hashCode();
  }

  @Override
  public String toString() {
    return repr.toUpperCase() + (humanRepr.isEmpty() ? "" : " (" + humanRepr + ")");
  }

}
