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

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.LessSyntax;


public class ColorOperationsFunctionsTest extends LessTestBase {

  @Test
  public void testContrast() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.FUNCTION_CALL);

    h.evalEquals("contrast(#fff, #111, #999, .5)", color("#111"));
    h.evalEquals("contrast(#222, #111, #999, .5)", color("#999"));
  }

  @Test
  public void testDarken() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.ADDITION);

    h.evalEquals("darken(#888, 50%) + #000", color("#080808"));
    h.evalEquals("darken(#f00, 40%) + #000", color("#330000"));
    h.evalEquals("darken(#fff, 5) + #000", color("#f2f2f2"));
    h.evalEquals("darken(darken(#fff, 0), 5) + #000", color("#f2f2f2"));
  }

  // TODO: testDesaturate

  // TODO: testFade

  // TODO: testFadeIn

  // TODO: testFadeOut

  // TODO: testGreyscale

  // TODO: testLighten

  // TODO: testMix

  // TODO: testSaturate

  // TODO: testShade

  // TODO: testSpin

  // TODO: testTint

}
