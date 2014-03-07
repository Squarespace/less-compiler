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

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Parselets;


public class FunctionCallTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.parseEquals("rgb(1,2,3)", call("rgb", dim(1), dim(2), dim(3)));
    h.parseEquals("foo-bar(@a)", call("foo-bar", var("@a")));

    Node str = quoted('"', false, "x", var("@y", true), "z");
    h.parseEquals("name(\"x@{y}z\")", call("name", str));

    Node foo1 = assign("foo", dim(1));
    Node bar2px = assign("bar", dim(2, Unit.PX));
    h.parseEquals("name(foo=1, bar=2px)", call("name", foo1, bar2px));
  }

}
