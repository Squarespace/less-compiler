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

import static com.squarespace.less.parse.Parselets.ASSIGNMENT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Unit;


public class AssignmentTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(assign("x", anon("y")), assign("x", anon("y")));

    assertNotEquals(assign("x", anon("y")), null);
    assertNotEquals(assign("x", anon("y")), assign("z", anon("y")));
    assertNotEquals(assign("x", anon("y")), assign("x", anon("z")));
  }

  @Test
  public void testModelReprSafety() {
    assign("x", anon("y")).toString();
  }

  @Test
  public void testAssignment() throws LessException {
    LessHarness h = new LessHarness(ASSIGNMENT);
    h.parseEquals("foo=1px", assign("foo", dim(1, Unit.PX)));
    h.parseEquals("foo=@bar", assign("foo", var("@bar")));
  }

}
