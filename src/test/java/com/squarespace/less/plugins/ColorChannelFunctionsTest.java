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
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.parse2.LessSyntax;


public class ColorChannelFunctionsTest extends LessTestBase {

  @Test
  public void testAlpha() throws LessException {
    LessHarness h = harness();

    h.evalEquals("alpha(rgba(10, 10, 10, .5))", dim(0.5));
    h.evalEquals("alpha(#aabbcc)", dim(1.0));
  }

  @Test
  public void testBlue() throws LessException {
    LessHarness h = harness();

    h.evalEquals("blue(rgb(50, 50, 37))", dim(37));
  }

  @Test
  public void testGreen() throws LessException {
    LessHarness h = harness();

    h.evalEquals("green(rgb(50, 37, 50))", dim(37));
  }

  // TODO: testHue

  // TODO: testLightness

  // TODO: testLuma

  @Test
  public void testRed() throws LessException {
    LessHarness h = harness();

    h.evalEquals("red(rgb(37, 50, 50))", dim(37));
  }

  // TODO: testSaturation

  private LessHarness harness() {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );
    return new LessHarness(LessSyntax.FUNCTION_CALL, defs);
  }

}
