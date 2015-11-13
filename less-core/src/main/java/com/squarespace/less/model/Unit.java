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
   * Mapping from a static unit's representation to its value.
   */
  private static final Map<String, Unit> UNIT_MAP = new HashMap<>();

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
   * Returns the current value of the unit identifier sequence.
   */
  public static int idSequence() {
    return idSequence;
  }

  /**
   * Get the unit for a given raw string.
   */
  public static Unit get(String raw) {
    String value = raw.toLowerCase();
    Unit unit = UNIT_MAP.get(value);
    return (unit == null) ? new Unit(value, value) : unit;
  }

  /**
   * Build a static unit definition.  These will be used far more often
   * than custom units, so we generate a unique id for fast comparisons.
   */
  public static Unit define(String repr, String humanRepr) {
    Unit unit = new Unit(idSequence++, repr, humanRepr);
    UNIT_MAP.put(repr, unit);
    return unit;
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
