package com.squarespace.less.match;

import static com.squarespace.less.model.Colors.TRANSPARENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.squarespace.less.core.LessUtils;
import com.squarespace.less.core.Pair;
import com.squarespace.less.model.Colors;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.Unit;

/**
 * Intern common values for different values found in stylesheets. This saves us
 * from two levels of memory allocation: substrings from the backing source file,
 * and constructing the AST nodes that wrap them.
 */
public class Interner {

  // Arrays of instances
  public static final RGBColor[] COLORS;
  public static final Dimension[] DIMENSIONS;
  public static final TextElement[] NULL_ELEMENTS;
  public static final TextElement[] DESC_ELEMENTS;
  public static final TextElement[] CHILD_ELEMENTS;
  public static final TextElement[] NAMESPACE_ELEMENTS;
  public static final TextElement[] SIB_ADJ_ELEMENTS;
  public static final TextElement[] SIB_GEN_ELEMENTS;
  public static final String[] FUNCTIONS;
  public static final Keyword[] KEYWORDS;
  public static final Property[] PROPERTIES;
  public static final Unit[] UNITS;

  // Double-array tries indexed to instances
  public static final DAT COLORS_DAT;
  public static final DAT DIMENSIONS_DAT;
  public static final DAT ELEMENT_DAT;
  public static final DAT FUNCTIONS_DAT;
  public static final DAT KEYWORD_DAT;
  public static final DAT PROPERTY_DAT;
  public static final DAT UNITS_DAT;

  // Fast lookup of color names by their integer value
  public static final int[] COLOR_NAME_INDEX;
  public static final String[] COLOR_NAMES;

  // Pattern for splitting value from units to construct Dimension instances
  private static final Pattern RE_DIM = Pattern.compile("([-+\\d\\.]+)([%\\w]+)?");

  static {
    try {
      // Load interned values for the various syntax fragments
      String[] _colors = load("colors.txt");
      String[] _elements = load("elements.txt");
      String[] _dimensions = load("dimensions.txt");
      String[] _functions = load("functions.txt");
      String[] _keywords = load("keywords.txt");
      String[] _properties = load("properties.txt");

      List<Pair<String, RGBColor>> colors = buildColors(_colors);

      // Construct syntax tree nodes or strings we'll use at runtime
      COLORS = colors.stream().map(e -> e.val()).toArray(RGBColor[]::new);
      DIMENSIONS = buildDimensions(_dimensions).stream().toArray(Dimension[]::new);
      NULL_ELEMENTS = withCombinator(null, _elements);
      DESC_ELEMENTS = withCombinator(Combinator.DESC, _elements);
      CHILD_ELEMENTS = withCombinator(Combinator.CHILD, _elements);
      NAMESPACE_ELEMENTS = withCombinator(Combinator.NAMESPACE, _elements);
      SIB_ADJ_ELEMENTS = withCombinator(Combinator.SIB_ADJ, _elements);
      SIB_GEN_ELEMENTS = withCombinator(Combinator.SIB_GEN, _elements);
      FUNCTIONS = _functions;
      KEYWORDS = Arrays.stream(_keywords).map(s -> new Keyword(s)).toArray(Keyword[]::new);
      PROPERTIES = Arrays.stream(_properties).map(s -> new Property(s)).toArray(Property[]::new);
      UNITS = Unit.values();

      // Build the double-array tries for fast lookups
      COLORS_DAT = build(colors.stream().map(e -> e.key()).collect(Collectors.toList()));
      DIMENSIONS_DAT = build(Arrays.asList(_dimensions));
      ELEMENT_DAT = build(Arrays.asList(_elements));
      FUNCTIONS_DAT = build(Arrays.asList(_functions));
      KEYWORD_DAT = build(Arrays.asList(_keywords));
      PROPERTY_DAT = build(Arrays.asList(_properties));
      UNITS_DAT = build(Arrays.stream(UNITS).map(u -> u.repr()).collect(Collectors.toList()));

      List<Pair<Integer, String>> named = buildNamedColors(colors);
      COLOR_NAME_INDEX = named.stream().mapToInt(e -> e.key().intValue()).toArray();
      COLOR_NAMES = named.stream().map(e -> e.val()).toArray(String[]::new);

    } catch (IOException e) {
      throw new RuntimeException("Interning raised an error", e);
    }
  }

  /**
   * Map the array of keys to build text elements with a given combinator.
   */
  private static TextElement[] withCombinator(Combinator comb, String[] keys) {
    return Arrays.stream(keys).map(s -> new TextElement(comb, s)).toArray(TextElement[]::new);
  }

  public static String colorToKeyword(RGBColor color) {
    if (color.alpha() != 1.0) {
      return null;
    }
    int value = (color.red() << 16) + (color.green() << 8) + color.blue();
    int i = Arrays.binarySearch(COLOR_NAME_INDEX, value);
    return i < 0 ? null : COLOR_NAMES[i];
  }

  /**
   * Build a double-array trie from the list of keys.
   */
  private static DAT build(List<String> keys) {
    DATBuilder builder = new DATBuilder(keys);
    builder.build();
    return new DAT(builder.base(), builder.check(), builder.indices());
  }

  /**
   * Parse dimensions.
   */
  public static List<Dimension> buildDimensions(String[] keys) {
    List<Dimension> result = new ArrayList<>();
    for (String key : keys) {
      Matcher m = RE_DIM.matcher(key);
      if (!m.matches()) {
        throw new RuntimeException("failed to parse dimension: " + key);
      }
      String _num = m.group(1);
      String _unit = m.group(2);
      Unit unit = null;
      if (_unit != null) {
        unit = Unit.get(_unit);
      }
      result.add(new Dimension(Double.parseDouble(_num), unit));
    }
    return result;
  }

  /**
   * Map color names and hex values to color instances.
   */
  public static List<Pair<String, RGBColor>> buildColors(String[] lines) {
    Map<String, RGBColor> unique = new HashMap<>();
    for (String line : lines) {
      String[] row = line.split("\\s+");
      String hex = row[0];
      if (row.length == 2) {
        String name = row[1];
        int[] rgb = Colors.hexToRGB(hex);
        RGBColor color = new RGBColor(rgb[0], rgb[1], rgb[2]);
        unique.put(name, color);
        unique.put(hex, color);
      } else {
        RGBColor color = RGBColor.fromHex(hex);
        unique.put(hex, color);
      }
    }

    List<Pair<String, RGBColor>> result = new ArrayList<>();
    for (Map.Entry<String, RGBColor> entry : unique.entrySet()) {
      result.add(Pair.of(entry.getKey(), entry.getValue()));
    }

    result.add(Pair.of(TRANSPARENT.keyword(), TRANSPARENT));
    return result;
  }

  /**
   * Map named colors to their integer value
   */
  public static List<Pair<Integer, String>> buildNamedColors(List<Pair<String, RGBColor>> colors) {
    List<Pair<Integer, String>> result = new ArrayList<>();
    for (Pair<String, RGBColor> elem : colors) {
      String key = elem.key();
      if (key.startsWith("#") || key.equals("transparent")) {
        continue;
      }
      RGBColor color = elem.val();
      int value = (color.red() << 16) + (color.green() << 8) + color.blue();
      result.add(Pair.of(value, key));
    }
    result.sort((a, b) -> Integer.compare(a.key(), b.key()));
    return result;
  }

  /**
   * Load a resource file, split and sort it.
   */
  public static String[] load(String name) throws IOException {
    String raw = LessUtils.readStream(Interner.class.getResourceAsStream(name));
    String[] res = raw.split("\n");
    return Arrays.stream(res).filter(e -> !e.isEmpty()).sorted().toArray(String[]::new);
  }

}
