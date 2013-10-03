package com.squarespace.v6.template.less.model;

import java.util.HashMap;
import java.util.Map;


public enum Unit {

  PERCENTAGE ("%"),
  
  // ABSOLUTE LENGTHS

  // centimeters
  CM ("cm"),
  // millimeters
  MM ("mm"),
  // inches (1in == 2.54cm)
  IN ("in"),
  // pixels (1px == 1/96in)
  PX ("px"),
  // points (1pt == 1/72in)
  PT ("pt"),
  // picas  (1pc == 12pt)
  PC ("pc"),
  
  // FONT-RELATIVE LENGTHS
  
  // width of the '0' (ZERO U+0030) glyph in the element's font
  CH ("ch"),
  // font size of element
  EM ("em"),
  // x-height of the element's font
  EX ("ex"),
  // font size of the root element
  REM ("rem"),
  
  // VIEWPORT-RELATIVE LENGTHS

  // 1% of viewport's height
  VH ("vh"),
  // 1% of viewport's width
  VW ("vw"),
  // 1% of viewport's smaller dimension
  VMIN ("vmin"),
  // 1% of viewport's larger dimension
  VMAX ("vmax"),
  // [bug / typo in less 1.3.3 'vm']
  VM ("vm"),
  
  
  // TIME
  
  // seconds
  S ("s"),
  // milliseconds
  MS ("ms"),
  
  // RESOLUTIONS
  
  // dots per inch
  DPI ("dpi"),
  // dots per centimeter
  DPCM ("dpcm"),
  // dots per 'px' unit (1dppx == 96dpi)
  DPPX ("dppx"),
  
  // FREQUENCIES
  
  // Hertz
  HZ ("hz"),
  // KiloHertz (1khz == 1000hz)
  KHZ ("khz"),
  
  // ANGLES
  
  // Degrees
  DEG ("deg"),
  // Gradians
  GRAD ("grad"),
  // Radians
  RAD ("rad"),
  // Turns
  TURN ("turn")
  
  ;

  // Keep this sorted roughly by most-used first
  // NOTE: 'vm' is added here to fix a bug in LESS. 
  public static final String REGEX = 
      "px|%|em|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn|dpi|dpcm|dppx|rem|vw|vh|vmin|vmax|ch|hz|khz|vm";
  
  private static final Map<String, Unit> UNIT_MAP = new HashMap<>();
  
  static {
    for (Unit unit : Unit.values()) {
      UNIT_MAP.put(unit.repr(), unit);
    }
  }
  
  private final String repr;
  
  private Unit(String repr) {
    this.repr = repr;
  }
  
  public String repr() {
    return repr;
  }
  
  public static Unit get(String raw) {
    return UNIT_MAP.get(raw.toLowerCase());
  }
  
  /**
   * Format a double to a unit-specific level of precision.
   */
  public String format(double val) {
    // XXX: precision
    return "";
  }
}
