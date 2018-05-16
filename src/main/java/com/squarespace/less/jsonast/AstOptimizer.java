/**
 * Copyright (c) 2018 SQUARESPACE, Inc.
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

package com.squarespace.less.jsonast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

public class AstOptimizer extends AstBuffer {

  /** Map from original string to index of triple */
  private final Map<String, Integer> entryMap = new HashMap<>();

  /** List of triples (original, escaped, score) */
  private final List<Entry> entries = new ArrayList<>();

  /** Reusable temporary buffer for string escaping */
  private final StringBuilder tmp = new StringBuilder();

  public Pair<Map<String, Integer>, List<String>> noOptimize() {
    List<String> table = entries.stream().map(e -> e.escaped).collect(Collectors.toList());
    return Pair.of(entryMap, table);
  }

  public Pair<Map<String, Integer>, List<String>> optimize() {
    // Sort by frequency descending
    entries.sort((a, b) -> Integer.compare(b.freq, a.freq));

    List<String> table = new ArrayList<>();
    Map<String, Integer> index = new HashMap<>();

    for (Entry entry : entries) {
      int offset = table.size();
      index.put(entry.original, offset);
      table.add(entry.escaped);
    }
    return Pair.of(index, table);
  }

  public String stats() {
    // Sort by frequency descending
    entries.sort((a, b) -> Integer.compare(b.freq, a.freq));
    StringBuilder buf = new StringBuilder();
    for (Entry entry : entries) {
      if (entry.freq > 1) {
        String s = entry.escaped;
        String row = String.format("%10d  %s\n", entry.freq,
            s == null ? "null" : s.substring(0, Math.min(s.length(), 40)));
        buf.append(row);
      }
    }
    return buf.toString();
  }

  /**
   * Add a string to the entry map, escaping it, or updating its frequency.
   */
  public void string(String original) {
    Integer id = entryMap.get(original);
    if (id == null) {
      id = entries.size();
      String escaped = escape(original);
      entryMap.put(original, id);
      entries.add(new Entry(original, escaped));
    } else {
      entries.get(id).freq++;
    }
  }

  // Mandatory escaped characters per JSON RFC 7159
  private static final String[] REPLACEMENTS = new String[0x1f];

  {
    // Quick-replace ASCII C0 controls
    for (int i = 0; i < 0x1f; i++) {
      REPLACEMENTS[i] = String.format("\\u%04x", i);
    }
  }

  /**
   * Escape a string for appending to a JSON stream.
   */
  private String escape(String s) {
    if (s == null) {
      return null;
    }
    tmp.setLength(0);
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"': tmp.append("\\\""); break;
        case '\\': tmp.append("\\\\"); break;
        case '\t': tmp.append("\\t"); break;
        case '\b': tmp.append("\\b"); break;
        case '\n': tmp.append("\\n"); break;
        case '\r': tmp.append("\\r");  break;
        case '\f': tmp.append("\\f"); break;

        // Fixes for eval(json) per https://github.com/google/gson/issues/341
        case '\u2028': tmp.append("\\u2028"); break;
        case '\u2029': tmp.append("\\u2029"); break;

        default:
          if (c < 0x20) {
            tmp.append(REPLACEMENTS[c]);
          } else {
            tmp.append(c);
          }
          break;
      }
    }
    return tmp.toString();
  }

  private static class Entry {

    public final String original;
    public final String escaped;

    /** Records frequency of occurrence of original string */
    public int freq;

    public Entry(String original, String escaped) {
      this.original = original;
      this.escaped = escaped;
      this.freq = 1;
    }

  }

}
