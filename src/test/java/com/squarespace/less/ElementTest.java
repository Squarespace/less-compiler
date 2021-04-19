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

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.model.Combinator.DESC;
import static com.squarespace.less.model.Combinator.SIB_ADJ;
import static com.squarespace.less.model.Combinator.SIB_GEN;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class ElementTest extends LessTestBase {

  @Test
  public void testElement() throws LessException {
    LessHarness h = new LessHarness(Parselets.ELEMENT);

    h.parseEquals("h1", element(DESC, "h1"));
    h.parseEquals(" h1", element(DESC, "h1"));
    h.parseEquals("*", element("*"));
    h.parseEquals(">foo", element(CHILD, "foo"));
    h.parseEquals("+:hover", element(SIB_ADJ, ":hover"));
    h.parseEquals("~.class-one", element(SIB_GEN, ".class-one"));
    h.parseEquals(">&", element(CHILD, "&"));

    // Attribute
    h.parseEquals("[foo]", attr(DESC, anon("foo")));
    h.parseEquals("[foo~=\"bar\"]", attr(DESC, anon("foo"), anon("~="), quoted('"', false, "bar")));

    // Variable
    h.parseEquals("@{a}", varelem(DESC, var("@a", true)));
    h.parseEquals(">@{foo}", varelem(CHILD, var("@foo", true)));
  }

  @Test
  public void testElement1() throws LessException {
    LessHarness h = new LessHarness(Parselets.ELEMENT);

    h.parseEquals(".mixin\\!tUp", element(DESC, ".mixin\\!tUp"));
    h.parseEquals("::-webkit-search-decoration", element(DESC, "::-webkit-search-decoration"));
  }

}
