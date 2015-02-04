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
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.Parselets;


public class ListFunctionsTest extends LessTestBase {

  @Test
  public void testLength() throws LessException {
    LessHarness h = harness();

    h.evalFails("length()", ARG_COUNT);

    h.evalEquals("length('foo')", dim(1));
    h.evalEquals("length(@str)", dim(1));
    h.evalEquals("length(@nums3)", dim(3));

    // LENGTH only considers the first argument.
    h.evalEquals("length(1, 2, 3)", dim(1));
  }

  @Test
  public void testExtract() throws LessException {
    LessHarness h = harness();

    h.evalFails("extract()", ARG_COUNT);
    h.evalFails("extract(1)", ARG_COUNT);

    h.evalEquals("extract(@nums3, 0)", dim(1));
    h.evalEquals("extract(@nums3, 1)", dim(2));
    h.evalEquals("extract(@nums3, 2)", dim(3));

    h.evalEquals("extract(@fruit, 0)", quoted('"', false, "orange"));
    h.evalEquals("extract(@fruit, 1)", quoted('"', false, "banana"));
    h.evalEquals("extract(@fruit, 2)", quoted('"', false, "apple"));
    h.evalEquals("extract(@fruit, 3)", quoted('"', false, "tomato"));

    // Failures to invoke will emit a literal representation of the call
    Node expected = call("extract", dim(1), dim(1));
    h.evalEquals("extract(1, 1)", expected);

    Node nums3 = expnlist(dim(1), dim(2), dim(3));
    h.evalEquals("extract(@nums3, -1)", call("extract", nums3, dim(-1)));
    h.evalEquals("extract(@nums3, 5)", call("extract", nums3, dim(5)));
  }

  private LessHarness harness() {
    ExpressionList fruit = expnlist(
        quoted('"', false, "orange"),
        quoted('"', false, "banana"),
        quoted('"', false, "apple"),
        quoted('"', false, "tomato")
      );
    GenericBlock defs = defs(
        def("@str", quoted('"', false, "hello")),
        def("@nums3", expnlist(dim(1), dim(2), dim(3))),
        def("@fruit", fruit)
    );
    return new LessHarness(Parselets.FUNCTION_CALL, defs);
  }

}
