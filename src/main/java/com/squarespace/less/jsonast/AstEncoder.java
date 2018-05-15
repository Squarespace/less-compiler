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

public class AstEncoder extends AstBuffer {

  /** Index of file paths */
  private final Map<String, Integer> pathIndex = new HashMap<>();

  /** Array of file paths. */
  private final List<String> paths = new ArrayList<>();

  /** Node positions in Base64 VLQ encoding */
  private final List<String> positions = new ArrayList<>();

  /** Buffer for emitting the JSON AST */
  private final StringBuilder buf = new StringBuilder();

  /** Index of original string -> offset of escaped string in table */
  private final Map<String, Integer> strings;

  /** Table of escaped strings sorted by frequency */
  private final List<String> table;

  private final boolean savePositions;

  public AstEncoder(Map<String, Integer> strings, List<String> table, boolean savePositions) {
    this.strings = strings;
    this.table = table;
    this.savePositions = savePositions;
  }

  /**
   * Create the final JSON bundle.
   */
  public String render() {
    // TODO: base64 vlq of char and line offsets and filename info
    StringBuilder out = new StringBuilder();
    out.append("{\"strings\":");
    renderStrings(out, table);

    if (savePositions) {
      out.append(",\"paths\":");
      renderStrings(out, paths);
      out.append(",\"positions\":");
      renderStrings(out, positions);
    }

    out.append(",\"root\":").append(buf);
    out.append("}");
    return out.toString();
  }

  protected void renderStrings(StringBuilder out, List<String> list) {
    out.append('[');
    int size = list.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        out.append(',');
      }
      String s = list.get(i);
      if (s == null) {
        out.append("null");
      } else {
        out.append('"').append(s).append('"');
      }
    }
    out.append(']');
  }

  @Override
  public void position(int line, int col, String path) {
    String key = base64vlq(line) + base64vlq(col);
    Integer id = pathIndex.get(path);
    if (id == null) {
      id = paths.size();
      pathIndex.put(path, id);
      paths.add(path);
    }
    key += base64vlq(id);
    positions.add(key);
  }

  public void string(String s) {
    Integer id = strings.get(s);
    buf.append(id);
  }

  public void append(long n) {
    buf.append(n);
  }

  public void append(double n) {
    buf.append(n);
  }

  public void append(char ch) {
    buf.append(ch);
  }

  public void append(String s) {
    buf.append(s);
  }

  private static final int VLQ_BASE_SHIFT = 5;
  private static final int VLQ_BASE = 1 << VLQ_BASE_SHIFT;
  private static final int VLQ_BASE_MASK = VLQ_BASE - 1;
  private static final int VLQ_CONT_BIT = VLQ_BASE;

  private static final char[] CHARS = new char[] {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
      'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
      'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

   private String base64vlq(int n) {
    StringBuilder buf = new StringBuilder();
    int vlq = vlqSigned(n);
    do {
      int digit = vlq & VLQ_BASE_MASK;
      vlq >>>= VLQ_BASE_SHIFT;
      if (vlq > 0) {
        digit |= VLQ_CONT_BIT;
      }
      buf.append(CHARS[digit]);
    } while (vlq > 0);

    return buf.toString();
  }

  private int vlqSigned(int n) {
    return n < 0 ? ((-n) << 1) + 1 : (n << 1) + 0;
  }
}
