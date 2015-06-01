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

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Parselets;


public class DetachedRulesetTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);

    Node expected = def("@foo", ruleset(
      rule(prop("color"), color("red")),
      rule(prop("font-size"), dim(12, Unit.PX))
    ));
    h.parseEquals("@foo: { color: red; font-size: 12px }", expected);
  }

  @Test
  public void testDereference() throws LessException {
    LessOptions opts = new LessOptions(true);
    LessHarness h = new LessHarness(Parselets.STYLESHEET);

    String source = "@foo: {color: red}; .parent { @foo(); }\n";
    String actual = h.execute(source, opts);
    assertEquals(actual, ".parent{color:red}");

    source = "@bar:red; @foo: {color: @bar;}; .parent { @bar: blue;  @other: @foo(); @other(); }\n";
    actual = h.execute(source, opts);
    assertEquals(actual, ".parent{color:red}");
  }

}
