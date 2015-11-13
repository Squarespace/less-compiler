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

import static com.squarespace.less.model.Units.DPI;
import static com.squarespace.less.model.Units.IN;
import static com.squarespace.less.model.Units.PX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class DimensionTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(dim(1), dim(1));
    assertEquals(dim(1, PX), dim(1, PX));
    assertEquals(dim(3.5, PX), dim(3.5, PX));

    assertNotEquals(dim(1), dim(2));
    assertNotEquals(dim(3.5), dim(5.3));
    assertNotEquals(dim(1), dim(1, PX));
    assertNotEquals(dim(1, IN), dim(1, PX));
    assertNotEquals(dim(3.5, IN), dim(3.5, PX));
    assertNotEquals(dim(3.5, IN), dim(3.5, PX));
  }

  @Test
  public void testModelReprSafety() {
    dim(1).toString();
    dim(1, PX).toString();
    dim(3.5, IN).toString();
  }

  @Test
  public void testDimension() throws LessException {
    LessHarness h = new LessHarness(Parselets.DIMENSION);

    h.parseEquals("0", dim(0));
    h.parseEquals("1.34", dim(1.34));
    h.parseEquals(".701", dim(0.701));
    h.parseEquals("0.7011", dim(0.7011));
    h.parseEquals("10px", dim(10, PX));
    h.parseEquals("30.1dpi", dim(30.1, DPI));
  }

}
