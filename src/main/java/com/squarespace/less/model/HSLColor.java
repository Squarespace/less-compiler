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

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;


/**
 * Color in the HSL colorspace.
 */
public class HSLColor extends BaseColor {

  /**
   * Hue value, 8-bit.
   */
  protected final double hue;

  /**
   * Saturation value, 8-bit.
   */
  protected final double saturation;

  /**
   * Lightness value, 8-bit.
   */
  protected final double lightness;

  /**
   * Alpha channel, 8-bit.
   */
  protected final double alpha;

  /**
   * Constructs an HSL color using the given values. Alpha channel defaults to 1.0.
   */
  public HSLColor(double hue, double saturation, double lightness) {
    this(hue, saturation, lightness, 1.0);
  }

  /**
   * Constructs an HSL color with the given HSL and alpha channel values.
   */
  public HSLColor(double hue, double saturation, double lightness, double alpha) {
    if (hue < 0 || hue > 1.0) {
      throw new LessInternalException("Serious error: something passing hue out of range: " + hue);
    }
    this.hue = clamp(hue * 360.0, 0.0, 360.0);
    this.saturation = clamp(saturation, 0.0, 1.0);
    this.lightness = clamp(lightness, 0.0, 1.0);
    this.alpha = clamp(alpha, 0.0, 1.0);
  }

  /**
   * Return the hue value.
   */
  public double hue() {
    return hue;
  }

  /**
   * Return the saturation value.
   */
  public double saturation() {
    return saturation;
  }

  /**
   * Return the lightness value.
   */
  public double lightness() {
    return lightness;
  }

  /**
   * Return the alpha channel value.
   */
  public double alpha() {
    return alpha;
  }

  /**
   * See {@link BaseColor#toRGB()}
   */
  @Override
  public RGBColor toRGB() {
    double r = 0;
    double g = 0;
    double b = 0;
    if (saturation == 0) {
      r = g = b = lightness;

    } else {
      double h = hue / 360.0;
      double l = lightness;
      double s = saturation;
      double q = l < 0.5 ? (l * (1 + s)) : (l + s - l * s);
      double p = 2 * l - q;
      r = hue(p, q, h + 1 / 3.0);
      g = hue(p, q, h);
      b = hue(p, q, h - 1 / 3.0);
    }
    return new RGBColor(r * 255.0, g * 255.0, b * 255.0, alpha);
  }

  /**
   * See {@link BaseColor#toHSL()}
   */
  @Override
  public HSLColor toHSL() {
    return this;
  }

  /**
   * See {@link BaseColor#getColorspace()}
   */
  @Override
  public Colorspace getColorspace() {
    return Colorspace.HSL;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    this.toRGB().repr(buf);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(getColorspace().toString()).append(' ').append(hue).append(' ');
    buf.append(saturation).append(' ').append(lightness).append(' ').append(alpha);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof HSLColor) {
      HSLColor other = (HSLColor)obj;
      return hue == other.hue
          && saturation == other.saturation
          && lightness == other.lightness
          && alpha == other.alpha;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  private double hue(double p, double q, double h) {
    if (h < 0) {
      h += 1.0;
    }
    if (h > 1) {
      h -= 1.0;
    }
    if (h < 1 / 6.0) {
      return p + (q - p) * 6.0 * h;
    }
    if (h < 1 / 2.0) {
      return q;
    }
    if (h < 2 / 3.0) {
      return p + (q - p) * ((2 / 3.0) - h) * 6.0;
    }
    return p;
  }

}
