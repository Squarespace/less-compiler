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

import java.util.HashMap;
import java.util.Map;

import com.squarespace.less.core.Chars;


public class Colors {

  public static final RGBColor BLACK = new RGBColor(0, 0, 0);

  public static final RGBColor WHITE = new RGBColor(0xff, 0xff, 0xff);

  private static final Map<String, int[]> NAME_TO_RGB = new HashMap<>();

  private static final Map<Integer, String> RGB_TO_NAME = new HashMap<>();

  static {
    add(0x00, "black");
    add(0xa9, "darkgray");
    add(0xa9, "darkgrey");
    add(0x69, "dimgray");
    add(0x69, "dimgrey");
    add(0xdc, "gainsboro");
    add(0x80, "gray");
    add(0x80, "grey");
    add(0xd3, "lightgray");
    add(0xd3, "lightgrey");
    add(0xc0, "silver");
    add(0xff, "white");
    add(0xf5, "whitesmoke");

    add(0xf0, 0xf8, 0xff, "aliceblue");
    add(0xfa, 0xeb, 0xd7, "antiquewhite");
    add(0x00, 0xff, 0xff, "aqua");
    add(0x7f, 0xff, 0xd4, "aquamarine");
    add(0xf0, 0xff, 0xff, "azure");
    add(0xf5, 0xf5, 0xdc, "beige");
    add(0xff, 0xe4, 0xc4, "bisque");
    add(0xff, 0xeb, 0xcd, "blanchedalmond");
    add(0x00, 0x00, 0xff, "blue");
    add(0x8a, 0x2b, 0xe2, "blueviolet");
    add(0xa5, 0x2a, 0x2a, "brown");
    add(0xde, 0xb8, 0x87, "burlywood");
    add(0x5f, 0x9e, 0xa0, "cadetblue");
    add(0x7f, 0xff, 0x00, "chartreuse");
    add(0xd2, 0x69, 0x1e, "chocolate");
    add(0xff, 0x7f, 0x50, "coral");
    add(0x64, 0x95, 0xed, "cornflowerblue");
    add(0xff, 0xf8, 0xdc, "cornsilk");
    add(0xdc, 0x14, 0x3c, "crimson");
    add(0x00, 0xff, 0xff, "cyan");
    add(0x00, 0x00, 0x8b, "darkblue");
    add(0x00, 0x8b, 0x8b, "darkcyan");
    add(0xb8, 0x86, 0x0b, "darkgoldenrod");
    add(0x00, 0x64, 0x00, "darkgreen");
    add(0xbd, 0xb7, 0x6b, "darkkhaki");
    add(0x8b, 0x00, 0x8b, "darkmagenta");
    add(0x55, 0x6b, 0x2f, "darkolivegreen");
    add(0xff, 0x8c, 0x00, "darkorange");
    add(0x99, 0x32, 0xcc, "darkorchid");
    add(0x8b, 0x00, 0x00, "darkred");
    add(0xe9, 0x96, 0x7a, "darksalmon");
    add(0x8f, 0xbc, 0x8f, "darkseagreen");
    add(0x48, 0x3d, 0x8b, "darkslateblue");
    add(0x2f, 0x4f, 0x4f, "darkslategray");
    add(0x2f, 0x4f, 0x4f, "darkslategrey");
    add(0x00, 0xce, 0xd1, "darkturquoise");
    add(0x94, 0x00, 0xd3, "darkviolet");
    add(0xff, 0x14, 0x93, "deeppink");
    add(0x00, 0xbf, 0xff, "deepskyblue");
    add(0x1e, 0x90, 0xff, "dodgerblue");
    add(0xb2, 0x22, 0x22, "firebrick");
    add(0xff, 0xfa, 0xf0, "floralwhite");
    add(0x22, 0x8b, 0x22, "forestgreen");
    add(0xff, 0x00, 0xff, "fuchsia");
    add(0xf8, 0xf8, 0xff, "ghostwhite");
    add(0xff, 0xd7, 0x00, "gold");
    add(0xda, 0xa5, 0x20, "goldenrod");
    add(0x00, 0x80, 0x00, "green");
    add(0xad, 0xff, 0x2f, "greenyellow");
    add(0xf0, 0xff, 0xf0, "honeydew");
    add(0xff, 0x69, 0xb4, "hotpink");
    add(0xcd, 0x5c, 0x5c, "indianred");
    add(0x4b, 0x00, 0x82, "indigo");
    add(0xff, 0xff, 0xf0, "ivory");
    add(0xf0, 0xe6, 0x8c, "khaki");
    add(0xe6, 0xe6, 0xfa, "lavender");
    add(0xff, 0xf0, 0xf5, "lavenderblush");
    add(0x7c, 0xfc, 0x00, "lawngreen");
    add(0xff, 0xfa, 0xcd, "lemonchiffon");
    add(0xad, 0xd8, 0xe6, "lightblue");
    add(0xf0, 0x80, 0x80, "lightcoral");
    add(0xe0, 0xff, 0xff, "lightcyan");
    add(0xfa, 0xfa, 0xd2, "lightgoldenrodyellow");
    add(0x90, 0xee, 0x90, "lightgreen");
    add(0xff, 0xb6, 0xc1, "lightpink");
    add(0xff, 0xa0, 0x7a, "lightsalmon");
    add(0x20, 0xb2, 0xaa, "lightseagreen");
    add(0x87, 0xce, 0xfa, "lightskyblue");
    add(0x77, 0x88, 0x99, "lightslategray");
    add(0x77, 0x88, 0x99, "lightslategrey");
    add(0xb0, 0xc4, 0xde, "lightsteelblue");
    add(0xff, 0xff, 0xe0, "lightyellow");
    add(0x00, 0xff, 0x00, "lime");
    add(0x32, 0xcd, 0x32, "limegreen");
    add(0xfa, 0xf0, 0xe6, "linen");
    add(0xff, 0x00, 0xff, "magenta");
    add(0x80, 0x00, 0x00, "maroon");
    add(0x66, 0xcd, 0xaa, "mediumaquamarine");
    add(0x00, 0x00, 0xcd, "mediumblue");
    add(0xba, 0x55, 0xd3, "mediumorchid");
    add(0x93, 0x70, 0xd8, "mediumpurple");
    add(0x3c, 0xb3, 0x71, "mediumseagreen");
    add(0x7b, 0x68, 0xee, "mediumslateblue");
    add(0x00, 0xfa, 0x9a, "mediumspringgreen");
    add(0x48, 0xd1, 0xcc, "mediumturquoise");
    add(0xc7, 0x15, 0x85, "mediumvioletred");
    add(0x19, 0x19, 0x70, "midnightblue");
    add(0xf5, 0xff, 0xfa, "mintcream");
    add(0xff, 0xe4, 0xe1, "mistyrose");
    add(0xff, 0xe4, 0xb5, "moccasin");
    add(0xff, 0xde, 0xad, "navajowhite");
    add(0x00, 0x00, 0x80, "navy");
    add(0xfd, 0xf5, 0xe6, "oldlace");
    add(0x80, 0x80, 0x00, "olive");
    add(0x6b, 0x8e, 0x23, "olivedrab");
    add(0xff, 0xa5, 0x00, "orange");
    add(0xff, 0x45, 0x00, "orangered");
    add(0xda, 0x70, 0xd6, "orchid");
    add(0xee, 0xe8, 0xaa, "palegoldenrod");
    add(0x98, 0xfb, 0x98, "palegreen");
    add(0xaf, 0xee, 0xee, "paleturquoise");
    add(0xd8, 0x70, 0x93, "palevioletred");
    add(0xff, 0xef, 0xd5, "papayawhip");
    add(0xff, 0xda, 0xb9, "peachpuff");
    add(0xcd, 0x85, 0x3f, "peru");
    add(0xff, 0xc0, 0xcb, "pink");
    add(0xdd, 0xa0, 0xdd, "plum");
    add(0xb0, 0xe0, 0xe6, "powderblue");
    add(0x80, 0x00, 0x80, "purple");
    add(0xff, 0x00, 0x00, "red");
    add(0xbc, 0x8f, 0x8f, "rosybrown");
    add(0x41, 0x69, 0xe1, "royalblue");
    add(0x8b, 0x45, 0x13, "saddlebrown");
    add(0xfa, 0x80, 0x72, "salmon");
    add(0xf4, 0xa4, 0x60, "sandybrown");
    add(0x2e, 0x8b, 0x57, "seagreen");
    add(0xff, 0xf5, 0xee, "seashell");
    add(0xa0, 0x52, 0x2d, "sienna");
    add(0x87, 0xce, 0xeb, "skyblue");
    add(0x6a, 0x5a, 0xcd, "slateblue");
    add(0x70, 0x80, 0x90, "slategray");
    add(0x70, 0x80, 0x90, "slategrey");
    add(0xff, 0xfa, 0xfa, "snow");
    add(0x00, 0xff, 0x7f, "springgreen");
    add(0x46, 0x82, 0xb4, "steelblue");
    add(0xd2, 0xb4, 0x8c, "tan");
    add(0x00, 0x80, 0x80, "teal");
    add(0xd8, 0xbf, 0xd8, "thistle");
    add(0xff, 0x63, 0x47, "tomato");
    add(0x40, 0xe0, 0xd0, "turquoise");
    add(0xee, 0x82, 0xee, "violet");
    add(0xf5, 0xde, 0xb3, "wheat");
    add(0xff, 0xff, 0x00, "yellow");
    add(0x9a, 0xcd, 0x32, "yellowgreen");
  }

  private static void add(int r, int g, int b, String name) {
    NAME_TO_RGB.put(name, new int[] { r, g, b });
    RGB_TO_NAME.put(rgbToInt(r, g, b), name);
  }

  private static void add(int c, String name) {
    add(c, c, c, name);
  }

  public static int[] nameToRGB(String name) {
    return NAME_TO_RGB.get(name);
  }

  public static String colorToName(RGBColor color) {
    return rgbToName(color.red(), color.green(), color.blue());
  }

  public static int[] hexToRGB(String raw) {
    int start = 0;
    int len = raw.length();
    while (start < len && raw.charAt(start) == Chars.NUMBER_SIGN) {
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
    return new int[] { c0, c1, c2 };
  }

  private static int rgbToInt(int r, int g, int b) {
    return (r << 16) + (g << 8) + b;
  }

  private static String rgbToName(int r, int g, int b) {
    return RGB_TO_NAME.get(rgbToInt(r, g, b));
  }

  private static int component(char ch) {
    return component(ch, ch);
  }

  private static int component(char ch0, char ch1) {
    return (Chars.hexvalue(ch0) << 4) + Chars.hexvalue(ch1);
  }

}
