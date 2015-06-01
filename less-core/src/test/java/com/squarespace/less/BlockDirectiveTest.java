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

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.Parselets;


public class BlockDirectiveTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule ruleXY = rule(prop("x"), anon("y"));
    Rule ruleXZ = rule(prop("x"), anon("z"));
    Block blockXY = block(ruleXY);
    Block blockXZ = block(ruleXZ);

    assertEquals(dir("x", blockXY), dir("x", blockXY));

    assertNotEquals(dir("x", blockXY), dir("y", blockXY));
    assertNotEquals(dir("x", blockXZ), dir("x", blockXY));
  }

  @Test
  public void testModelReprSafety() {
    dir("x", block(rule(prop("x"), anon("y")))).toString();
  }

  @Test
  public void testBlockDirective() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);

    Stylesheet sheet = stylesheet();
    sheet.add(dir("@page foo", block(rule(prop("a"), dim(1)))));
    h.parseEquals("@page foo { a: 1; }", sheet);

    sheet = stylesheet();
    sheet.add(dir("@namespace", expn(kwd("foo"), kwd("bar"))));
    h.parseEquals("@namespace foo bar;", sheet);
  }

}
