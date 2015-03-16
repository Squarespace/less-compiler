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
import static com.squarespace.less.model.CombinatorType.SIB_GEN;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class ElementTest extends LessTestBase {

  @Test
  public void testElement() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTOR);

    h.parseEquals("h1", selector(element("h1")));
    h.parseEquals(" h1", selector(element("h1")));
    h.parseEquals("*", selector(element("*")));
    h.parseEquals(">foo", selector(comb(CHILD), element("foo")));
    h.parseEquals("+:hover", selector(comb(SIB_ADJ), element(":hover")));
    h.parseEquals("~.class-one", selector(comb(SIB_GEN), element(".class-one")));
    h.parseEquals(">&", selector(comb(CHILD), element("&")));

    // Attribute
    h.parseEquals("[foo]", selector(attr(anon("foo"))));
    h.parseEquals("[foo~=\"bar\"]", selector(attr(anon("foo"), anon("~="), quoted('"', false, "bar"))));

    // Variable
    h.parseEquals("@{a}", selector(element(var("@a", true))));
    h.parseEquals(">@{foo}", selector(comb(CHILD), element(var("@foo", true))));
  }

}
