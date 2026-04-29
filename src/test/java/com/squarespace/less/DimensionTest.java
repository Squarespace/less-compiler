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

package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.compat.Patch;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.LessSyntax;


public class DimensionTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(dim(1), dim(1));
    assertEquals(dim(1, Unit.PX), dim(1, Unit.PX));
    assertEquals(dim(3.5, Unit.PX), dim(3.5, Unit.PX));

    assertNotEquals(dim(1), dim(2));
    assertNotEquals(dim(3.5), dim(5.3));
    assertNotEquals(dim(1), dim(1, Unit.PX));
    assertNotEquals(dim(1, Unit.IN), dim(1, Unit.PX));
    assertNotEquals(dim(3.5, Unit.IN), dim(3.5, Unit.PX));
    assertNotEquals(dim(3.5, Unit.IN), dim(3.5, Unit.PX));
  }

  @Test
  public void testModelReprSafety() {
    dim(1).toString();
    dim(1, Unit.PX).toString();
    dim(3.5, Unit.IN).toString();
  }

  @Test
  public void testDimension() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.DIMENSION);

    h.parseEquals("2.3PX", dim(2.3, Unit.PX));

    h.parseEquals("0", dim(0));
    h.parseEquals("20", dim(20));
    h.parseEquals("1.34", dim(1.34));
    h.parseEquals(".701", dim(0.701));
    h.parseEquals("0.7011", dim(0.7011));
    h.parseEquals("10px", dim(10, Unit.PX));
    h.parseEquals("30.1dpi", dim(30.1, Unit.DPI));
    h.parseEquals("+30.1dpi", dim(30.1, Unit.DPI));
    h.parseEquals("-30.1dpi", dim(-30.1, Unit.DPI));
    h.parseEquals("2.3PX", dim(2.3, Unit.PX));
    h.parseEquals("2.3CM", dim(2.3, Unit.CM));

    // Patch.NUMBER_EXPO: the released grammar (default level) stops a
    // number at 'e'/'E'. The exponent becomes a stray identifier (pinned
    // in testRepr and CompatPatchTest.testNumberExpo, since the leftover
    // tokens fail a bare-DIMENSION complete()). The fixed level reads a
    // single CSS number, e.g. 1e2 == 100.
    LessOptions fixed = new LessOptions();
    fixed.compatLevel(Patch.maxThreshold());
    h.parseEquals("1e2", dim(100), fixed);
    h.parseEquals("1E2", dim(100), fixed);
    h.parseEquals("2E2", dim(200), fixed);
    h.parseEquals("1e2px", dim(100, Unit.PX), fixed);
    h.parseEquals("1.5e3", dim(1500), fixed);
    h.parseEquals("1.5e3px", dim(1500, Unit.PX), fixed);
    h.parseEquals("1.5e-2", dim(0.015), fixed);
    h.parseEquals("1e-1", dim(0.1), fixed);
    h.parseEquals("+1e2", dim(100), fixed);
    h.parseEquals("-1e2", dim(-100), fixed);
    h.parseEquals("-1.5e-2", dim(-0.015), fixed);
    h.parseEquals("1e2%", dim(100, Unit.PERCENTAGE), fixed);

    // em/ex are units, not exponents, at every level.
    h.parseEquals("1em", dim(1, Unit.EM));
    h.parseEquals("1em", dim(1, Unit.EM), fixed);
    h.parseEquals("1.5ex", dim(1.5, Unit.EX));
    h.parseEquals("1.5ex", dim(1.5, Unit.EX), fixed);

    // units starting with e are not mistaken for exponents
    h.parseEquals("1em", dim(1, Unit.EM));
    h.parseEquals("1ex", dim(1, Unit.EX));

    h.parseFails("apx", SyntaxErrorType.INCOMPLETE_PARSE);
    h.parseFails(".x", SyntaxErrorType.INCOMPLETE_PARSE);
    h.parseFails("1.", SyntaxErrorType.INCOMPLETE_PARSE);
    h.parseFails("-1.", SyntaxErrorType.INCOMPLETE_PARSE);
    h.parseFails("--1.3", SyntaxErrorType.INCOMPLETE_PARSE);
  }

  @Test
  public void testRepr() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.RULE);

    h.renderEquals("foo: -1.5/3", "foo: -.5");
    h.renderEquals("foo: 8/-63333333333333333333333333333;", "foo: 0");

    // Exponent notation: the released level renders the split tokens
    // literally. The fixed level renders the plain value (Patch.NUMBER_EXPO).
    LessOptions released = new LessOptions();
    released.compatLevel(0);
    h.renderEquals("foo: 1e2;", "foo: 1 e2", released);
    LessOptions fixed = new LessOptions();
    fixed.compatLevel(Patch.maxThreshold());
    h.renderEquals("foo: 1e2;", "foo: 100", fixed);
    h.renderEquals("foo: 1e2px;", "foo: 100px", fixed);
    h.renderEquals("foo: 1.5e-2;", "foo: .015", fixed);
  }

}
