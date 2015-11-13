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


/**
 * A color in the RGB colorspace.
 */
public class RGBColor extends BaseColor {

  /**
   * Permutation values used for HSV to RGB conversion.
   */
  private static final int[][] HSV_PERMUTATIONS = new int[][] {
    new int[] { 0, 3, 1 },
    new int[] { 2, 0, 1 },
    new int[] { 1, 0, 3 },
    new int[] { 1, 2, 0 },
    new int[] { 3, 1, 0 },
    new int[] { 0, 1, 2 }
  };

  /**
   * Red color channel, 8-bit.
   */
  protected final int red;

  /**
   * Green color channel, 8-bit
   */
  protected final int green;

  /**
   * Blue color channel, 8-bit.
   */
  protected final int blue;

  /**
   * Alpha channel, 0 - 1.0
   */
  protected final double alpha;

  /**
   * Color was constructed from a keyword.
   */
  protected final boolean fromKeyword;

  /**
   * Force hex representation with output.
   */
  protected boolean forceHex;

  /**
   * Constructs an RGB color from double values.
   */
  public RGBColor(double red, double green, double blue) {
    this(red, green, blue, 1.0);
  }

  /**
   * Constructs an RGB color from double values, with an alpha channel.
   */
  public RGBColor(double red, double green, double blue, double alpha) {
    this((int)Math.round(red), (int)Math.round(green), (int)Math.round(blue), alpha);
  }

  /**
   * Constructs an RGB color from integer values.
   */
  public RGBColor(int red, int green, int blue) {
    this(red, green, blue, 1.0);
  }

  /**
   * Constructs an RGB color from integer values, setting the flag indicating
   * whether this color was defined by a keyword.
   */
  public RGBColor(int red, int green, int blue, boolean fromKeyword) {
    this(red, green, blue, 1.0, fromKeyword);
  }

  /**
   * Constructs an RGB color from integer values, with an alpha channel.
   */
  public RGBColor(int red, int green, int blue, double alpha) {
    this(red, green, blue, alpha, false);
  }

  /**
   * Constructs an RGB color from integer values, with an alpha channel,
   * setting the flag indicating whether this color was defined by a keyword.
   */
  public RGBColor(int red, int green, int blue, double alpha, boolean fromKeyword) {
    this.red = (int)clamp(red, 0, 255);
    this.green = (int)clamp(green, 0, 255);
    this.blue = (int)clamp(blue, 0, 255);
    this.alpha = clamp(alpha, 0.0, 1.0);
    this.fromKeyword = fromKeyword;
  }

  /**
   * Copy the color.
   */
  public RGBColor copy() {
    return new RGBColor(red, green, blue, alpha, fromKeyword);
  }

  /**
   * Return the color's colorspace.
   */
  public Colorspace getColorspace() {
    return Colorspace.RGB;
  }

  /**
   * Return the value for the red channel.
   */
  public int red() {
    return red;
  }

  /**
   * Return the value for the green channel.
   */
  public int green() {
    return green;
  }

  /**
   * Return the value for the blue channel.
   */
  public int blue() {
    return blue;
  }

  /**
   * Return the value for the alpha channel.
   */
  public double alpha() {
    return alpha;
  }

  /**
   * Compute the luma value for this color.
   */
  public double luma() {
    return (0.2126 * (red / 255.0) + 0.7152 * (green / 255.0) + 0.0722 * (blue / 255.0)) * alpha;
  }

  /**
   * Indicates whether this color was defined by a keyword.
   */
  public boolean fromKeyword() {
    return fromKeyword && alpha == 1.0;
  }

  /**
   * Indicates whether this color's output should be forced to hexadecimal.
   */
  public boolean forceHex() {
    return forceHex && alpha == 1.0;
  }

  /**
   * Sets the flag indicating whether this color's output should be forced to
   * hexadecimal.
   */
  public void forceHex(boolean flag) {
    this.forceHex = flag;
  }

  /**
   * Converts this color to RGB colorspace.
   */
  @Override
  public RGBColor toRGB() {
    return this;
  }

  /**
   * Converts this color to the HSL colorspace.
   */
  @Override
  public HSLColor toHSL() {
    double r = red / 255.0;
    double g = green / 255.0;
    double b = blue / 255.0;

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

  /**
   * Converts this color to ARGB representation.
   */
  public Anonymous toARGB() {
    StringBuilder buf = new StringBuilder();
    int alpha = (int)Math.round(this.alpha * 255);
    buf.append('#');
    hexdigit(buf, alpha);
    hexdigit(buf, red);
    hexdigit(buf, green);
    hexdigit(buf, blue);
    return new Anonymous(buf.toString());
  }

  /**
   * Appends a hexadecimal digit to the given buffer.
   */
  private static void hexdigit(StringBuilder buf, int num) {
    buf.append(hexchar(num >> 4)).append(hexchar(num & 0x0F));
  }

  /**
   * Constructs an RGB color instance from HSVA values.
   */
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

  /**
   * Emits the color's representation, which may be in compressed or expanded
   * hexadecimal form, or a keyword.
   *
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {

    if (alpha < 1.0) {
      buf.append("rgba(").append(red).listSep();
      buf.append(green).listSep();
      buf.append(blue).listSep();
      formatDouble(buf, alpha);
      buf.append(')');

    } else {
      char r0 = hexchar(red >> 4);
      char r1 = hexchar(red & 0x0F);
      char g0 = hexchar(green >> 4);
      char g1 = hexchar(green & 0x0F);
      char b0 = hexchar(blue >> 4);
      char b1 = hexchar(blue & 0x0F);

      // Check if the color can be emitted as a 3-character hex sequence.
      boolean hex3 = r0 == r1 && g0 == g1 && b0 == b1;

      if (!forceHex) {
        // Check if an equivalent color keyword exists that is shorter
        // than its hex string. Some examples: red < #f00 beige < #f5f5dc.
        // If so, we output the keyword.
        String name = Colors.colorToName(this);
        if (name != null) {
          int len = name.length();
          if ((hex3 && len <= 4) || (!hex3 && len <= 7)) {
            buf.append(name);
            return;
          }
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

  /**
   * See {@link Node#modelRepr(Buffer)}
   * @param buf
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(getColorspace().toString()).append(' ');
    buf.append(red).append(' ').append(green).append(' ').append(blue).append(' ').append(alpha);
    if (fromKeyword) {
      buf.append(" [from keyword]");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RGBColor) {
      RGBColor other = (RGBColor)obj;
      return red == other.red
          && green == other.green
          && blue == other.blue
          && alpha == other.alpha
          && fromKeyword == other.fromKeyword;
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      return buildHashCode(red, green, blue, alpha);
    }
    return hashCode;
  }

}
