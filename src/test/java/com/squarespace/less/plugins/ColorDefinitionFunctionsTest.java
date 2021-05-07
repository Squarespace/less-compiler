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

  private LessHarness harness() {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );

    return new LessHarness(LessSyntax.FUNCTION_CALL, defs);
  }

}
