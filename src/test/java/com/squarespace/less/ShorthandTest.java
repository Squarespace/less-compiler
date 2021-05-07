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


public class ShorthandTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(shorthand(dim(1), dim(2)), shorthand(dim(1), dim(2)));

    assertNotEquals(shorthand(dim(1), dim(2)), null);
    assertNotEquals(shorthand(dim(1), dim(2)), dim(2));
    assertNotEquals(shorthand(dim(1), dim(2)), shorthand(dim(3), dim(4)));
  }

  @Test
  public void testModelReprSafety() {
    shorthand(dim(1), dim(2, Unit.PX)).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.SHORTHAND);

    h.parseEquals("small/12px", shorthand(kwd("small"), dim(12, Unit.PX)));
    h.parseEquals("15vm/23vm", shorthand(dim(15, Unit.VM), dim(23, Unit.VM)));
    h.parseEquals("foo/3.14", shorthand(kwd("foo"), dim(3.14)));
    h.parseEquals("@a/@b", shorthand(var("@a"), var("@b")));
  }

}
