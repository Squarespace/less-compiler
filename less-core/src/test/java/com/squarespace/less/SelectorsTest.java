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


public class SelectorsTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(selectors(selector(element("h1"))), selectors(selector(element("h1"))));

    assertNotEquals(selectors(selector(element("h1"))), null);
    assertNotEquals(selectors(selector(element("h1"))), selector(element("foo")));
    assertNotEquals(selectors(selector(element("h1"))), selectors(selector(element("h2"))));
  }

  @Test
  public void testModelReprSafety() {
    selectors(selector(element("a")), selector(element("y"), element("z"))).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTORS);

    h.parseEquals("a, b",
        selectors(selector(element("a")), selector(element("b"))));

    h.parseEquals("a > b, c + d",
        selectors(
            selector(element("a"), comb(CHILD), element("b")),
            selector(element("c"), comb(SIB_ADJ), element("d"))
        ));
  }

}
