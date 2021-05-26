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
import com.squarespace.less.model.KeywordColor;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.Unit;

/**
 * Intern pool contains a set of common values found across many stylesheets. For example
 * the keyword "none" can occur many times in a stylesheet. When this keyword is encountered
 * we need to construct a Keyword node to place it in the AST. Instead of allocating a new
 * node we can do a fast lookup in the intern pool and reuse that instance multiple times.
 *
 * This saves us from two levels of memory allocation: copying substrings from the backing
 * source file, and constructing the AST nodes that wrap them. It also avoids some deeper
 * parsing for certain types, like decimal numbers and hex colors.
 *
 * TODO: we could pre-process these sets and serialize them faster to improve initialization time.
 */
public class InternPool {

  // Arrays of instances
  protected static final RGBColor[] COLORS_HEX;
  protected static final RGBColor[] COLORS_KEYWORD;
  protected static final Dimension[] DIMENSIONS;
  protected static final TextElement[] NULL_ELEMENTS;
  protected static final TextElement[] DESC_ELEMENTS;
  protected static final TextElement[] CHILD_ELEMENTS;
  protected static final TextElement[] NAMESPACE_ELEMENTS;
  protected static final TextElement[] SIB_ADJ_ELEMENTS;
  protected static final TextElement[] SIB_GEN_ELEMENTS;
  protected static final String[] FUNCTIONS;
  protected static final Node[] KEYWORDS;
  protected static final Property[] PROPERTIES;
  protected static final Unit[] UNITS;

  // Double-array tries indexed to instances
  protected static final DAT COLORS_HEX_DAT;
  protected static final DAT COLORS_KEYWORD_DAT;
  protected static final DAT DIMENSIONS_DAT;
  protected static final DAT ELEMENT_DAT;
  protected static final DAT FUNCTIONS_DAT;
  protected static final DAT KEYWORD_DAT;
  protected static final DAT PROPERTY_DAT;
  protected static final DAT UNITS_DAT;

  // Fast lookup of color names by their integer value
  protected static final int[] COLOR_NAME_INDEX;
  protected static final String[] COLOR_NAMES;

  // Pattern for splitting value from units to construct Dimension instances
  private static final Pattern RE_DIM = Pattern.compile("([-+\\d\\.]+)([%\\w]+)?");

  static {
//    long start = System.nanoTime();
    try {
      // Load interned values for the various syntax fragments
      String[] _colors = load("colors.txt");
      String[] _elements = load("elements.txt");
      String[] _dimensions = load("dimensions.txt");
      String[] _functions = load("functions.txt");
      String[] _keywords = load("keywords.txt");
      String[] _properties = load("properties.txt");

      // Separate hex from keyword colors
      List<Pair<String, RGBColor>> colors_hex = buildColors(_colors, true);
      List<Pair<String, RGBColor>> colors_kwd = buildColors(_colors, false);

      // Copy color keywords and merge in plain keywords
      List<Pair<String, Node>> keywords = buildKeywords(colors_kwd, _keywords);

      // Construct syntax tree nodes or strings we'll use at runtime
      COLORS_HEX = colors_hex.stream().map(e -> e.val()).toArray(RGBColor[]::new);
      COLORS_KEYWORD = colors_kwd.stream().map(e -> e.val()).toArray(RGBColor[]::new);
      DIMENSIONS = buildDimensions(_dimensions).stream().toArray(Dimension[]::new);
      NULL_ELEMENTS = withCombinator(null, _elements);
      DESC_ELEMENTS = withCombinator(Combinator.DESC, _elements);
      CHILD_ELEMENTS = withCombinator(Combinator.CHILD, _elements);
      NAMESPACE_ELEMENTS = withCombinator(Combinator.NAMESPACE, _elements);
      SIB_ADJ_ELEMENTS = withCombinator(Combinator.SIB_ADJ, _elements);
      SIB_GEN_ELEMENTS = withCombinator(Combinator.SIB_GEN, _elements);
      FUNCTIONS = _functions;
      KEYWORDS = keywords.stream().map(e -> e.val()).toArray(Node[]::new);
      PROPERTIES = Arrays.stream(_properties).map(s -> new Property(s)).toArray(Property[]::new);
      UNITS = Unit.values();

      // Build the double-array tries for fast lookups
      COLORS_HEX_DAT = build(colors_hex.stream().map(e -> e.key()).collect(Collectors.toList()));
      COLORS_KEYWORD_DAT = build(colors_kwd.stream().map(e -> e.key()).collect(Collectors.toList()));
      DIMENSIONS_DAT = build(Arrays.asList(_dimensions));
      ELEMENT_DAT = build(Arrays.asList(_elements));
      FUNCTIONS_DAT = build(Arrays.asList(_functions));
      KEYWORD_DAT = build(keywords.stream().map(e -> e.key()).collect(Collectors.toList()));
      PROPERTY_DAT = build(Arrays.asList(_properties));
      UNITS_DAT = build(Arrays.stream(UNITS).map(u -> u.repr()).collect(Collectors.toList()));

      // Binary search of color integer values to the corresponding names.
      List<Pair<Integer, String>> named = buildNamedColors(colors_kwd);
      COLOR_NAME_INDEX = named.stream().mapToInt(e -> e.key().intValue()).toArray();
      COLOR_NAMES = named.stream().map(e -> e.val()).toArray(String[]::new);

    } catch (IOException e) {
      throw new RuntimeException("Interning raised an error", e);
    }
//    long elapsed = System.nanoTime() - start;
//    System.err.println("intern pool initialized in " + (elapsed / 1000000) + " ms");
  }

  /**
   * Lookup a Property in the intern pool or construct a new one.
   */
  public Property property(String raw, int start, int end) {
    int ix = PROPERTY_DAT.get(raw, start, end);
    if (ix == -1) {
      return new Property(raw.substring(start, end));
    }
    return PROPERTIES[ix];
  }

  /**
   * Lookup a Keyword in the intern pool or construct a new one. This contains
   * both color and plain keywords.
   */
  public Node keyword(String raw, int start, int end) {
    int ix = KEYWORD_DAT.get(raw, start, end);
    if (ix == -1) {
      return new Keyword(raw.substring(start, end));
    }
    return KEYWORDS[ix];
  }

  /**
   * Lookup a dimension Unit in the intern pool.
   */
  public Unit unit(String raw, int start, int end) {
    int ix = UNITS_DAT.get(raw, start, end);
    if (ix == -1) {
      String rep = raw.substring(start, end);
      return Unit.get(rep);
    }
    return UNITS[ix];
  }

  /**
   * Lookup a Dimension in the intern pool or return null if not found,
   */
  public Dimension dimension(String raw, int start, int end) {
    int ix = DIMENSIONS_DAT.get(raw, start, end);
    return ix == -1 ? null : DIMENSIONS[ix];
  }

  /**
   * Lookup a TextElement in the intern pool for the given Combinator. If not
   * found a new one is constructed.
   */
  public TextElement element(Combinator comb, String raw, int start, int end) {
    int ix = ELEMENT_DAT.get(raw, start, end);
    if (ix == -1) {
      return new TextElement(comb, raw.substring(start, end));
    }
    if (comb == null) {
      return NULL_ELEMENTS[ix];
    }
    switch (comb) {
      case CHILD:
        return CHILD_ELEMENTS[ix];
      case NAMESPACE:
        return NAMESPACE_ELEMENTS[ix];
      case SIB_ADJ:
        return SIB_ADJ_ELEMENTS[ix];
      case SIB_GEN:
        return SIB_GEN_ELEMENTS[ix];
      case DESC:
      default:
        return DESC_ELEMENTS[ix];
    }
  }

  /**
   * Lookup a function name in the intern pool or copy the substring.
   */
  public String function(String raw, int start, int end) {
    int ix = FUNCTIONS_DAT.getIgnoreCase(raw, start, end);
    if (ix == -1) {
      return raw.substring(start, end).toLowerCase();
    }
    return FUNCTIONS[ix];
  }

  /**
   * Lookup a hex or keyword color in the intern pool.
   */
  public RGBColor color(String raw, int start, int end) {
    if (raw.charAt(start) == '#') {
      int ix = COLORS_HEX_DAT.getIgnoreCase(raw, start, end);
      return ix == -1 ? RGBColor.fromHex(raw.substring(start, end)) : COLORS_HEX[ix];
    }

    // Note: we only intern lowercase colors since there is some overlap between
    // capitalized keywords in font names and we want to avoid adjusting the case,
    // e.g. "font-family: Crimson Text;" would become "font-family: crimson Text;"
    int ix = COLORS_KEYWORD_DAT.get(raw, start, end);
    return ix == -1 ? null : COLORS_KEYWORD[ix];
  }

  /**
   * Lookup a keyword color in the intern pool or return null if not found.
   */
  public RGBColor keywordColor(String raw, int start, int end) {
    int ix = InternPool.COLORS_KEYWORD_DAT.get(raw, start, end);
    return ix == -1 ? null : InternPool.COLORS_KEYWORD[ix];
  }

  /**
   * Given a color, return its CSS name or null if none exists.
   */
  public String colorToKeyword(RGBColor color) {
    if (color.alpha() != 1.0) {
      return null;
    }
    int value = (color.red() << 16) + (color.green() << 8) + color.blue();
    int i = Arrays.binarySearch(COLOR_NAME_INDEX, value);
    return i < 0 ? null : COLOR_NAMES[i];
  }

  /**
   * Merge color and plain keywords.
   */
  private static List<Pair<String, Node>> buildKeywords(List<Pair<String, RGBColor>> colors, String[] keywords) {
    List<Pair<String, Node>> result = new ArrayList<>();
    for (Pair<String, RGBColor> color : colors) {
      result.add(Pair.of(color.key(), (Node)color.val()));
    }
    for (String keyword : keywords) {
      if (keyword.equals("transparent")) {
        continue;
      }
      result.add(Pair.of(keyword, new Keyword(keyword)));
    }
    return result;
  }

  /**
   * Map the array of keys to build text elements with a given combinator.
   */
  private static TextElement[] withCombinator(Combinator comb, String[] keys) {
    return Arrays.stream(keys).map(s -> new TextElement(comb, s)).toArray(TextElement[]::new);
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
  private static List<Dimension> buildDimensions(String[] keys) {
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
   * Map color names and hex values to color instances. We index them separately
   * since we want to do case-insensitive matching on hex colors, and case-sensitive
   * on keyword colors.
   */
  private static List<Pair<String, RGBColor>> buildColors(String[] lines, boolean hex_only) {
    Map<String, RGBColor> unique = new HashMap<>();
    for (String line : lines) {
      String[] row = line.split("\\s+");
      String hex = row[0];
      if (row.length == 2) {
        int[] rgb = Colors.hexToRGB(hex);
        if (hex_only) {
          RGBColor color = new RGBColor(rgb[0], rgb[1], rgb[2]);
          unique.put(hex, color);
        } else {
          String name = row[1];
          RGBColor color = new KeywordColor(swapNames(name), rgb[0], rgb[1], rgb[2]);
          unique.put(name, color);
        }
      } else {
        if (hex_only) {
          RGBColor color = RGBColor.fromHex(hex);
          unique.put(hex, color);
        }
      }
    }

    List<Pair<String, RGBColor>> result = new ArrayList<>();
    for (Map.Entry<String, RGBColor> entry : unique.entrySet()) {
      result.add(Pair.of(entry.getKey(), entry.getValue()));
    }

    if (!hex_only) {
      // Add special color-like keyword "transparent"
      result.add(Pair.of(TRANSPARENT.keyword(), TRANSPARENT));
    }
    return result;
  }

  /**
   * Force resolved keywords containing "gray" to the "grey" form,
   * for backwards-compatibility.
   */
  private static String swapNames(String name) {
    if (name.indexOf("gray") != -1) {
      return name.replace("gray", "grey");
    }
    return name;
  }

  /**
   * Map named colors to their integer value
   */
  private static List<Pair<Integer, String>> buildNamedColors(List<Pair<String, RGBColor>> colors) {
    List<Pair<Integer, String>> result = new ArrayList<>();
    for (Pair<String, RGBColor> elem : colors) {
      String key = elem.key();
      if (key.equals("transparent")) {
        continue;
      }

      // Checks for backwards-compatibility

      // Prefer "grey" over "gray".
      if (key.contains("gray")) {
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
  private static String[] load(String name) throws IOException {
    String raw = LessUtils.readStream(InternPool.class.getResourceAsStream(name));
    String[] res = raw.split("\n");
    return Arrays.stream(res).filter(e -> !e.isEmpty()).sorted().toArray(String[]::new);
  }

}
