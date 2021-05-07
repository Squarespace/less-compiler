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

import com.squarespace.less.core.LessTestBase;


public class PropertyTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(prop("x"), prop("x"));

    assertNotEquals(prop("x"), null);
    assertNotEquals(prop("x"), anon("x"));
    assertNotEquals(prop("x"), prop("y"));
  }

  @Test
  public void testModelReprSafety() {
    prop("x").toString();
  }

  // DISABLED: this fragment is parsed in a different way
  @Test
  public void testParse() throws LessException {
//    LessHarness h = new LessHarness(LessSyntax.PROPERTY);
//
//    h.parseEquals("a", prop("a"));
//    h.parseEquals("-moz-foo-bar", prop("-moz-foo-bar"));
//    h.parseEquals("*-foo", prop("*-foo"));
//    h.parseEquals("-", prop("-"));
//
//    h.parseFails("A", INCOMPLETE_PARSE);
  }

}
