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

package com.squarespace.less.plugins;

import static com.squarespace.less.ExecuteErrorType.INVALID_ARG;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.parse.LessSyntax;


public class ColorDefinitionFunctionsTest extends LessTestBase {

  @Test
  public void testAverage() throws LessException {
    LessHarness h = harness();

    h.evalEquals("average(#888, #444)", color("#666"));
    h.evalEquals("average(#000, #888)", color("#444"));
  }

  @Test
  public void testARGB() throws LessException {
    LessHarness h = harness();

    h.evalEquals("argb(rgba(1, 2, 3, 50%))", anon("#80010203"));
  }

  @Test
  public void testRGB() throws LessException {
    LessHarness h = harness();

    h.evalEquals("rgb(@one, 2, @three)", color("#010203"));
    h.evalEquals("rgb(1000, 1000, 1000)", color("#fff"));
    h.evalFails("rgb('foo', 2, 3)", INVALID_ARG);
  }

  @Test
  public void testRGBA() throws LessException {
    LessHarness h = harness();

    h.evalEquals("rgba(1, 2, 3, .5)", rgb(1, 2, 3, 0.5));
    h.evalEquals("rgba(@one, @one, @one, @one)", rgb(1, 1, 1, 1));
    h.evalFails("rgba(1, 1, 1, 'foo')", INVALID_ARG);
  }

  @Test
  public void testHueUnits() throws LessException {
    LessHarness h = harness();

    // CSS hue angles with units are valid. The numeric value is the
    // degree count, so 360deg == 0deg and larger angles wrap.
    h.evalEquals("hsl(360deg, 100%, 50%)", hsl(0.0, 1.0, 0.5));
    h.evalEquals("hsl(450deg, 100%, 50%)", hsl(0.25, 1.0, 0.5));
    h.evalEquals("hsla(120deg, 100%, 50%, 100%)", hsla(1.0 / 3.0, 1.0, 0.5, 1.0));
    h.evalEquals("hsv(360deg, 100%, 100%)", color("#f00"));
    h.evalEquals("hsva(240deg, 100%, 100%, 100%)", color("#00f"));

    // Out-of-range and negative hues wrap into [0, 360).
    h.evalEquals("hsl(720deg, 100%, 50%)", hsl(0.0, 1.0, 0.5));
    h.evalEquals("hsl(-30deg, 100%, 50%)", hsl(330.0 / 360.0, 1.0, 0.5));
    h.evalEquals("hsl(-30, 100%, 50%)", hsl(330.0 / 360.0, 1.0, 0.5));
    h.evalEquals("hsla(-30, 50%, 50%, 50%)", hsla(330.0 / 360.0, 0.5, 0.5, 0.5));
    h.evalEquals("hsv(-30, 100%, 100%)", color("#ff0080"));
    h.evalEquals("hsva(-30, 100%, 100%, 50%)", rgb(255, 0, 128, 0.5));

    // Hue still must be a number.
    h.evalFails("hsl('foo', 100%, 50%)", INVALID_ARG);
  }

  @Test
  public void testHueRenderParityWith172() throws LessException {
    LessHarness h = harness();

    // In-range integer hues at full saturation must render byte-identically
    // to 1.7.2. The two-pass hue wrap ((x % m) + m) % m perturbed in-range
    // hues by 1 ULP and flipped the 8-bit channel rounding for 23/360 hues
    // (e.g. hue 30 was #ff8000 in 1.7.2 and #ff7f00 after the two-pass
    // wrap). Pinning the rendered output catches that class of regression,
    // which the hue-field comparisons in testHueUnits cannot.
    h.renderEquals("hsl(30, 100%, 50%)", "#ff8000");
    h.renderEquals("hsv(30, 100%, 100%)", "#ff8000");

    // All 23 hsl and 29 hsv hues whose 8-bit output is sensitive to a
    // 1-ULP hue perturbation, pinned to the 1.7.2 rendered values.
    String[] hslFlips = {
        "2:#ff0900", "6:#ff1a00", "10:#ff2b00", "30:#ff8000", "50:#ffd500",
        "70:#d4ff00", "74:#c3ff00", "94:#6eff00", "102:#4cff00", "118:#08ff00",
        "126:#00ff19", "142:#00ff5e", "146:#00ff6f", "162:#00ffb3", "186:#00e5ff",
        "190:#00d4ff", "194:#00c3ff", "218:#005eff", "234:#0019ff", "238:#0008ff",
        "282:#b300ff", "306:#ff00e6", "350:#ff002b"
    };
    for (String row : hslFlips) {
      String[] parts = row.split(":");
      h.renderEquals(String.format("hsl(%s, 100%%, 50%%)", parts[0]), parts[1]);
    }

    String[] hsvFlips = {
        "26:#ff6f00", "30:#ff8000", "50:#ffd500", "54:#ffe600", "70:#d4ff00",
        "74:#c3ff00", "78:#b3ff00", "82:#a2ff00", "86:#91ff00", "106:#3cff00",
        "122:#00ff08", "126:#00ff19", "130:#00ff2a", "142:#00ff5e", "162:#00ffb3",
        "166:#00ffc3", "174:#00ffe5", "186:#00e5ff", "194:#00c3ff", "210:#0080ff",
        "218:#005eff", "238:#0008ff", "258:#4c00ff", "282:#b300ff", "302:#ff00f7",
        "306:#ff00e6", "326:#ff0090", "346:#ff003c", "350:#ff002b"
    };
    for (String row : hsvFlips) {
      String[] parts = row.split(":");
      h.renderEquals(String.format("hsv(%s, 100%%, 100%%)", parts[0]), parts[1]);
    }
  }

  private LessHarness harness() {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );

    return new LessHarness(LessSyntax.FUNCTION_CALL, defs);
  }

}
