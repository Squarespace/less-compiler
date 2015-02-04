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
import static com.squarespace.less.ExecuteErrorType.UNKNOWN_UNIT;
import static com.squarespace.less.core.Constants.TRUE;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Parselets;


public class MiscFunctionsTest extends LessTestBase {

  @Test
  public void testColor() throws LessException {
    LessHarness h = harness();

    // Color hex parsing
    h.evalEquals("color('#fff')", color("#fff"));
    h.evalEquals("color('#010203')", color("#010203"));
    h.evalEquals("color('#aabbcc')", rgb(0xaa, 0xbb, 0xcc));
  }

  // TODO: testDataUri

  // TODO: testDefault

  @Test
  public void testGetUnit() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalEquals("get-unit(1px)", kwd("px"));
    h.evalEquals("get-unit(1%)", quoted('"', false, "%"));
    h.evalEquals("get-unit(1)", anon(""));

    // Composing unit-oriented function calls
    h.evalEquals("isunit(unit(1, get-unit(1%)), '%')", TRUE);
  }

  // TODO: testSvgGradient

  @Test
  public void testUnit() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalEquals("unit(1, px)", dim(1, Unit.PX));
    h.evalEquals("unit(1, 'px')", dim(1, Unit.PX));
    h.evalEquals("unit(3em, pt)", dim(3, Unit.PT));
    h.evalEquals("unit(3em)", dim(3));

    h.evalFails("unit('foo', px)", INVALID_ARG);
    h.evalFails("unit(1px, #f00)", UNKNOWN_UNIT);
    h.evalFails("unit(3em, quark)", UNKNOWN_UNIT);
  }

  private LessHarness harness() {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );
    return new LessHarness(Parselets.FUNCTION_CALL, defs);
  }

}
