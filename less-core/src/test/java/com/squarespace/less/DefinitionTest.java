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
import com.squarespace.less.model.Units;
import com.squarespace.less.parse.Parselets;


public class DefinitionTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(def("@x", anon("y")), def("@x", anon("y")));
    assertNotEquals(def("@y", anon("y")), def("@x", anon("y")));
    assertNotEquals(def("@x", anon("y")), def("@y", anon("y")));
  }

  @Test
  public void testModelReprSafety() {
    def("@x", anon("y")).toString();
  }

  @Test
  public void testDefinition() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);

    h.parseEquals("@foo: 12px", def("@foo", dim(12, Units.PX)));
    h.parseEquals("@foo: 'bar';", def("@foo", quoted('\'', false, "bar")));
    h.parseEquals("@a: @b;", def("@a", var("@b")));
    h.parseEquals("@bpMedium: 1000px;", def("@bpMedium", dim(1000, Units.PX)));
  }

}
