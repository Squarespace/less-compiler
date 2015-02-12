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

import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.less.model.PropertyMergeMode.COMMA;
import static com.squarespace.less.model.PropertyMergeMode.NONE;
import static com.squarespace.less.model.PropertyMergeMode.SPACE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class PropertyTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(prop("x"), prop("x"));

    assertNotEquals(prop("x"), null);
    assertNotEquals(prop("x"), anon("x"));
    assertNotEquals(prop("x"), prop("y"));

    assertEquals(prop("x", COMMA), prop("x", COMMA));
    assertNotEquals(prop("x", COMMA), prop("x", NONE));
    assertNotEquals(prop("x", COMMA), prop("x", SPACE));
  }

  @Test
  public void testModelReprSafety() {
    prop("x").toString();
  }

  @Test
  public void testRepr() {
    assertEquals(prop("x").repr(), "x");
    assertEquals(prop("x", NONE).repr(), "x");
    assertEquals(prop("x", COMMA).repr(), "x+");
    assertEquals(prop("x", SPACE).repr(), "x+_");
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.PROPERTY);

    h.parseEquals("a", prop("a"));
    h.parseEquals("-moz-foo-bar", prop("-moz-foo-bar"));
    h.parseEquals("*-foo", prop("*-foo"));

    h.parseFails("A", INCOMPLETE_PARSE);
  }


}
