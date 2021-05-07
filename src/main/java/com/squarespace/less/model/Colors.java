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

import com.squarespace.less.core.Chars;


/**
 * Holds the mappings for all named CSS colors, and static instances of
 * frequently-used colors BLACK and WHITE.
 */
public class Colors {

  /**
   * The keyword "transparent" acts like a color in certain contexts, but is
   * not strictly a color.
   */
  public static final TransparentColor TRANSPARENT = new TransparentColor();

  /**
   * CSS color black.
   */
  public static final RGBColor BLACK = new RGBColor(0, 0, 0);

  /**
   * CSS color white.
   */
  public static final RGBColor WHITE = new RGBColor(0xff, 0xff, 0xff);

  /**
   * Parses a hexadecimal color string into the corresponding RGB color value.
   * Throws an exception if the string is invalid.
   */
  public static int[] hexToRGB(String raw) {
    int[] rgb = new int[] { 0, 0, 0 };
    hexToRGB(raw, 0, raw.length(), rgb);
    return rgb;
  }

  public static void hexToRGB(String raw, int start, int end, int[] rgb) {
    int len = end - start;
    while (start < end && raw.charAt(start) == Chars.NUMBER_SIGN) {
      len--;
      start++;
    }

    int c0 = 0;
    int c1 = 0;
    int c2 = 0;
    if (len == 3) {
      c0 = component(raw.charAt(start + 0));
      c1 = component(raw.charAt(start + 1));
      c2 = component(raw.charAt(start + 2));

    } else if (len == 6) {
      c0 = component(raw.charAt(start + 0), raw.charAt(start + 1));
      c1 = component(raw.charAt(start + 2), raw.charAt(start + 3));
      c2 = component(raw.charAt(start + 4), raw.charAt(start + 5));

    } else {
      throw new IllegalArgumentException("Color strings must consist of 3 or 6 hex characters");
    }
    rgb[0] = c0;
    rgb[1] = c1;
    rgb[2] = c2;
  }

  /**
   * Returns the integer value for the given doubled hexadecimal character.
   * Calling {@code component('A')} will return 0xAA.
   */
  private static int component(char ch) {
    return component(ch, ch);
  }

  /**
   * Returns the integer value for the given hexadecimal character.
   * Calling {@code component('8', 'A')} will return 0x8A.
   */
  private static int component(char ch0, char ch1) {
    return (Chars.hexvalue(ch0) << 4) + Chars.hexvalue(ch1);
  }

}
