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
  }

}
