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

package com.squarespace.less.core;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * Support class for formatting error messages flexibly using a Map.
 */
public class MapFormat {

  /**
   * Pattern used to extract the keys from the format string. These will end
   * up being the keys in the resulting map.
   */
  private static final Pattern KEY_ESCAPE = Pattern.compile("%\\([a-zA-Z][a-zA-Z0-9]*\\)");

  /**
   * Default for {@link #nullPlaceholder}
   */
  private static final String EMPTY_STRING = "";

  /**
   * Keys in the map.
   */
  private final List<String> keys = new ArrayList<>();

  /**
   * Format string used to construct the message. See {@link #apply(Map)}
   */
  private final String format;

  /**
   * String to be emitted if a value is {@code null}
   */
  private final String nullPlaceholder;

  /**
   * Constructs an instance using the given format string.
   */
  public MapFormat(String raw) {
    this(raw, EMPTY_STRING);
  }

  /**
   * Constructs an instance using the given format string and null placeholder.
   */
  public MapFormat(String raw, String nullPlaceholder) {
    this.nullPlaceholder = nullPlaceholder;
    this.format = init(raw);
  }

  /**
   * Extracts the keys from the pattern and generates the format string.
   */
  private String init(String raw) {
    StringBuffer buf = new StringBuffer();
    Matcher matcher = KEY_ESCAPE.matcher(raw);
    int mark = 0;
    int length = raw.length();
    while (matcher.find()) {
      int start0 = matcher.start();
      if (mark < start0) {
        buf.append(raw.substring(mark, start0));
      }
      buf.append(raw.charAt(start0));
      keys.add(raw.substring(start0 + 2, matcher.end() - 1));
      mark = matcher.end();
    }
    if (mark < length) {
      buf.append(raw.substring(mark, length));
    }
    return buf.toString();
  }

  /**
   * Returns the internal format string.
   */
  public String getFormat() {
    return format;
  }

  /**
   * Applies the {@code params} to producing the formatted message.
   */
  public String apply(Map<String, Object> params) {
    List<Object> values = new ArrayList<>(params.size());
    int size = keys.size();
    for (int i = 0; i < size; i++) {
      String key = keys.get(i);
      Object obj = params.get(key);
      if (obj == null) {
        obj = nullPlaceholder;
      }
      values.add(obj);
    }
    return StringEscapeUtils.escapeJava(String.format(format, values.toArray()));
  }
}