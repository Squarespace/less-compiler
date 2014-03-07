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

package com.squarespace.less.plugins;

import static com.squarespace.less.ExecuteErrorType.ARG_COUNT;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.Parselets;


public class TestFunctionsTest extends LessTestBase {

  @Test
  public void testFunction() throws LessException {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );

    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL, defs);

    // Non-built-in functions.
    Node expected = call("foo", dim(1));
    h.evalEquals("foo(1)", expected);
    h.evalEquals("foo(0.5 + 0.5)", expected);
    h.evalEquals("foo(@one)", expected);
    h.evalEquals("foo(2-@one)", expected);

    // Assignment args
    expected = call("bar", assign("name", quoted('"', false, "bob")));
    h.evalEquals("bar(name=\"bob\")", expected);

    // wrong arg counts
    h.evalFails("dummy3(1, 2, 3, 4)", ARG_COUNT);
    h.evalFails("dummy3()", ARG_COUNT);
  }

}
