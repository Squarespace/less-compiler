package com.squarespace.less.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.Unit;

/**
 * Captures statistics of counts and sizes of interned values, allowing
 * us to estimate the overall memory savings. This is intended to only
 * be used during development, to assess the impact of the intern pool
 * on memory savings by parsing large sets of bulk stylesheets.
 */
public class StatsInternPool extends InternPool {

  private final Stats property_stats = new Stats();
  private final Stats keyword_stats = new Stats();
  private final Stats unit_stats = new Stats();
  private final Stats dimension_stats = new Stats();
  private final Stats element_stats = new Stats();
  private final Stats function_stats = new Stats();
  private final Stats color_hex_stats = new Stats();
  private final Stats color_keyword_stats = new Stats();

  private final Map<String, Integer> properties = new HashMap<>();
  private final Map<String, Integer> keywords = new HashMap<>();
  private final Map<String, Integer> elements = new HashMap<>();

  /**
   * Produce a report of the number of interned values and the percentage
   * of the source that were interned.
   */
  public String report(long source_length, int top_values) {
    Stats total = new Stats();
    total.merge(property_stats);
    total.merge(keyword_stats);
    total.merge(unit_stats);
    total.merge(dimension_stats);
    total.merge(element_stats);
    total.merge(function_stats);
    total.merge(color_hex_stats);
    total.merge(color_keyword_stats);

    StringBuilder buf = new StringBuilder();
    buf.append("total source parsed: ").append(commas(source_length)).append("\n\n");
    stats(buf, "property", property_stats, source_length);
    stats(buf, "element", element_stats, source_length);
    stats(buf, "dimension", dimension_stats, source_length);
    stats(buf, "keyword", keyword_stats, source_length);
    stats(buf, "unit", unit_stats, source_length);
    stats(buf, "function", function_stats, source_length);
    stats(buf, "color hex", color_hex_stats, source_length);
    stats(buf, "color keyword", color_keyword_stats, source_length);
    stats(buf, "total", total, source_length);

    top(buf, "property", properties, top_values);
    top(buf, "keyword", keywords, top_values);
    top(buf, "element", elements, top_values);
    return buf.toString();
  }

  private void stats(StringBuilder buf, String name, Stats stats, long source_length) {
    String hit_pct = String.format("%.2f %%", percent(stats.hit_size, source_length));
    String miss_pct = String.format("%.2f %%", percent(stats.miss_size, source_length));
    String hit_rate = String.format("%.2f %%", percent(stats.hit_count, stats.hit_count + stats.miss_count));
    buf.append(name).append(":\n");
    buf.append("      hits: ").append(commas(stats.hit_count)).append('\n');
    buf.append("    misses: ").append(commas(stats.miss_count)).append('\n');
    buf.append("  hit rate: ").append(hit_rate).append('\n');
    buf.append("   hit len: ").append(commas(stats.hit_size)).append(" bytes (").append(hit_pct).append(")\n");
    buf.append("  miss len: ").append(commas(stats.miss_size)).append(" bytes (").append(miss_pct).append(")\n");
    buf.append('\n');
  }

  private void top(StringBuilder buf, String name, Map<String, Integer> map, int num) {
    List<Map.Entry<String, Integer>> entries = map.entrySet().stream()
        .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())).collect(Collectors.toList());
    int limit = Math.min(num, entries.size());
    buf.append("Top ").append(name).append(" values:\n");
    for (int i = 0; i < limit; i++) {
      Map.Entry<String, Integer> entry = entries.get(i);
      String fmt = String.format("  %10d  %s\n", entry.getValue(), entry.getKey());
      buf.append(fmt);
    }
    buf.append('\n');
  }

  private double percent(long n, long d) {
    return (n / (double)d) * 100.0;
  }

  private String commas(long n) {
    return String.format("%,d", n);
  }

  @Override
  public Property property(String raw, int start, int end) {
    int len = end - start;
    int ix = PROPERTY_DAT.get(raw, start, end);
    if (ix == -1) {
      property_stats.miss(len);
      String value = raw.substring(start, end);
      properties.put(value, properties.getOrDefault(value, 0) + 1);
      return new Property(value);
    }
    property_stats.hit(len);
    return PROPERTIES[ix];
  }

  @Override
  public Node keyword(String raw, int start, int end) {
    int len = end - start;
    int ix = KEYWORD_DAT.get(raw, start, end);
    if (ix == -1) {
      keyword_stats.miss(len);
      String value = raw.substring(start, end);
      keywords.put(value, keywords.getOrDefault(value, 0) + 1);
      return new Keyword(value);
    }
    keyword_stats.hit(len);
    return KEYWORDS[ix];
  }

  @Override
  public Unit unit(String raw, int start, int end) {
    int len = end - start;
    int ix = UNITS_DAT.get(raw, start, end);
    if (ix == -1) {
      unit_stats.miss(len);
      String rep = raw.substring(start, end);
      return Unit.get(rep);
    }
    unit_stats.hit(len);
    return UNITS[ix];
  }

  @Override
  public Dimension dimension(String raw, int start, int end) {
    int len = end - start;
    int ix = DIMENSIONS_DAT.get(raw, start, end);
    if (ix == -1) {
      dimension_stats.miss(len);
      return null;
    }
    dimension_stats.hit(len);
    return DIMENSIONS[ix];
  }

  @Override
  public TextElement element(Combinator comb, String raw, int start, int end) {
    int len = end - start;
    int ix = ELEMENT_DAT.get(raw, start, end);
    if (ix == -1) {
      element_stats.miss(len);
      String value = raw.substring(start, end);
      elements.put(value, elements.getOrDefault(value, 0) + 1);
      return new TextElement(comb, value);
    }
    element_stats.hit(len);
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

  @Override
  public String function(String raw, int start, int end) {
    int len = end - start;
    int ix = FUNCTIONS_DAT.getIgnoreCase(raw, start, end);
    if (ix == -1) {
      function_stats.miss(len);
      return raw.substring(start, end).toLowerCase();
    }
    function_stats.hit(len);
    return FUNCTIONS[ix];
  }

  @Override
  public RGBColor color(String raw, int start, int end) {
    int len = end - start;
    if (raw.charAt(start) == '#') {
      int ix = COLORS_HEX_DAT.getIgnoreCase(raw, start, end);
      if (ix == -1) {
        color_hex_stats.miss(len);
        return RGBColor.fromHex(raw.substring(start, end));
      }
      color_hex_stats.hit(len);
      return COLORS_HEX[ix];
    }

    // Note: we only intern lowercase colors since there is some overlap between
    // capitalized keywords in font names and we want to avoid adjusting the case,
    // e.g. "font-family: Crimson Text;" would become "font-family: crimson Text;"
    int ix = COLORS_KEYWORD_DAT.get(raw, start, end);
    if (ix == -1) {
      color_keyword_stats.miss(len);
      return null;
    }
    color_keyword_stats.hit(len);
    return COLORS_KEYWORD[ix];
  }

  @Override
  public RGBColor keywordColor(String raw, int start, int end) {
    int len = end - start;
    int ix = COLORS_KEYWORD_DAT.get(raw, start, end);
    if (ix == -1) {
      color_keyword_stats.miss(len);
      return null;
    }
    color_keyword_stats.hit(len);
    return COLORS_KEYWORD[ix];
  }

  public static class Stats {

    public int hit_count = 0;
    public long hit_size = 0;
    public int miss_count = 0;
    public long miss_size = 0;

    public void hit(int len) {
      this.hit_count++;
      this.hit_size += len;
    }

    public void miss(int len) {
      this.miss_count++;
      this.miss_size += len;
    }

    public void merge(Stats s) {
      this.hit_count += s.hit_count;
      this.hit_size += s.hit_size;
      this.miss_count += s.miss_count;
      this.miss_size += s.miss_size;
    }
  }
}
