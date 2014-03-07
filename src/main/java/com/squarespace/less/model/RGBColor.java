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

import static com.squarespace.less.core.Chars.hexchar;

import com.squarespace.less.core.Buffer;


public class RGBColor extends BaseColor {

  private static final int[][] HSV_PERMUTATIONS = new int[][] {
    new int[] { 0, 3, 1 },
    new int[] { 2, 0, 1 },
    new int[] { 1, 0, 3 },
    new int[] { 1, 2, 0 },
    new int[] { 3, 1, 0 },
    new int[] { 0, 1, 2 }
  };

  private final int c0;

  private final int c1;

  private final int c2;

  private final double alpha;

  private final boolean keyword;

  private boolean forceHex;

  public RGBColor(double c0, double c1, double c2) {
    this(c0, c1, c2, 1.0);
  }

  public RGBColor(double c0, double c1, double c2, double alpha) {
    this((int)Math.round(c0), (int)Math.round(c1), (int)Math.round(c2), alpha);
  }

  public RGBColor(int c0, int c1, int c2) {
    this(c0, c1, c2, 1.0);
  }

  public RGBColor(int c0, int c1, int c2, boolean keyword) {
    this(c0, c1, c2, 1.0, keyword);
  }

  public RGBColor(int c0, int c1, int c2, double alpha) {
    this(c0, c1, c2, alpha, false);
  }

  public RGBColor(int c0, int c1, int c2, double alpha, boolean keyword) {
    this.c0 = (int)clamp(c0, 0, 255);
    this.c1 = (int)clamp(c1, 0, 255);
    this.c2 = (int)clamp(c2, 0, 255);
    this.alpha = clamp(alpha, 0.0, 1.0);
    this.keyword = keyword;
  }

  public RGBColor copy() {
    return new RGBColor(c0, c1, c2, alpha, keyword);
  }

  public Colorspace getColorspace() {
    return Colorspace.RGB;
  }

  public int red() {
    return c0;
  }

  public int green() {
    return c1;
  }

  public int blue() {
    return c2;
  }

  public double alpha() {
    return alpha;
  }

  public double luma() {
    return (0.2126 * (c0 / 255.0) + 0.7152 * (c1 / 255.0) + 0.0722 * (c2 / 255.0)) * alpha;
  }

  public boolean keyword() {
    return keyword && alpha == 1.0;
  }

  public boolean forceHex() {
    return forceHex && alpha == 1.0;
  }

  public void forceHex(boolean flag) {
    this.forceHex = flag;
  }

  @Override
  public RGBColor toRGB() {
    return this;
  }

  @Override
  public HSLColor toHSL() {
    double r = c0 / 255.0;
    double g = c1 / 255.0;
    double b = c2 / 255.0;

    double max = Math.max(Math.max(r, g), b);
    double min = Math.min(Math.min(r, g), b);
    double h = 0.0;
    double s = 0.0;
    double d = max - min;
    double l = (max + min) / 2.0;

    if (max == min) {
      h = s = 0.0;
    } else {
      s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
      if ((max - r) == 0.0) {
        h = (g - b) / d + (g < b ? 6 : 0);
      } else if ((max - g) == 0.0) {
        h = (b - r) / d + 2;
      } else if ((max - b) == 0.0) {
        h = (r - g) / d + 4;
      }
      h /= 6.0;
    }
    return new HSLColor(h, s, l, alpha);
  }

  public Anonymous toARGB() {
    StringBuilder buf = new StringBuilder();
    int alpha = (int)Math.round(this.alpha * 255);
    buf.append('#');
    hexdigit(buf, alpha);
    hexdigit(buf, c0);
    hexdigit(buf, c1);
    hexdigit(buf, c2);
    return new Anonymous(buf.toString());
  }

  private static void hexdigit(StringBuilder buf, int num) {
    buf.append(hexchar(num >> 4)).append(hexchar(num & 0x0F));

  }
  public static RGBColor fromHSVA(double hue, double saturation, double value, double alpha) {
    hue *= 360;
    int i = (int)Math.floor((hue / 60) % 6);
    double f = (hue / 60.0) - i;
    double[] values = new double[] {
        value,
        value * (1 - saturation),
        value * (1 - f * saturation),
        value * (1 - (1 - f) * saturation)
    };
    double red = values[HSV_PERMUTATIONS[i][0]] * 255.0;
    double green = values[HSV_PERMUTATIONS[i][1]] * 255.0;
    double blue = values[HSV_PERMUTATIONS[i][2]] * 255.0;
    return new RGBColor(red, green, blue, alpha);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RGBColor) {
      RGBColor other = (RGBColor)obj;
      return c0 == other.c0
          && c1 == other.c1
          && c2 == other.c2
          && alpha == other.alpha
          && keyword == other.keyword;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public void repr(Buffer buf) {

    if (alpha < 1.0) {
      buf.append("rgba(").append(c0).listSep();
      buf.append(c1).listSep();
      buf.append(c2).listSep();
      formatDouble(buf, alpha);
      buf.append(')');

    } else {
      char r0 = hexchar(c0 >> 4);
      char r1 = hexchar(c0 & 0x0F);
      char g0 = hexchar(c1 >> 4);
      char g1 = hexchar(c1 & 0x0F);
      char b0 = hexchar(c2 >> 4);
      char b1 = hexchar(c2 & 0x0F);

      // Check if the color can be emitted as a 3-character hex sequence.
      boolean hex3 = (r0 == r1 && g0 == g1 && b0 == b1);

      // Check if an equivalent color keyword exists that is shorter
      // than its hex string. Some examples: red < #f00 beige < #f5f5dc.
      // If so, we output the keyword.
      String name = Colors.colorToName(this);
      if (!forceHex && name != null) {
        int len = name.length();
        if ((hex3 && len <= 4) || (!hex3 && len <= 7)) {
          buf.append(name);
          return;
        }
      }

      buf.append('#');
      if (hex3) {
        buf.append(r0).append(g0).append(b0);
      } else {
        buf.append(r0).append(r1).append(g0).append(g1).append(b0).append(b1);
      }
    }
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(getColorspace().toString()).append(' ');
    buf.append(c0).append(' ').append(c1).append(' ').append(c2).append(' ').append(alpha);
    if (keyword) {
      buf.append(" [from keyword]");
    }
  }

}
