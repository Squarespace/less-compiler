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

import static com.squarespace.less.model.CombinatorType.CHILD;
import static com.squarespace.less.model.CombinatorType.SIB_ADJ;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class AttributeElementTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(attr("x"), attr("x"));
    assertEquals(attr("y", anon("z")), attr("y", anon("z")));

    assertNotEquals(attr("y", anon("z")), attr(anon("y")));
  }

  @Test
  public void testModelReprSafety() {
    attr("x", "y").toString();
    attr("y").toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTOR);

    h.parseEquals(" [class]", selector(attr(anon("class"))));

    h.parseEquals(">[foo=\"bar\"]",
        selector(comb(CHILD), attr(anon("foo"), anon("="), quoted('"', false, "bar"))));

    h.parseEquals("+[a=\"@{bar}\"]",
        selector(comb(SIB_ADJ), attr(anon("a"), anon("="), quoted('"', false, var("@bar", true)))));
  }

}
