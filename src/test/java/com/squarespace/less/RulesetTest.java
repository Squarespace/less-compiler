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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse2.LessSyntax;


public class RulesetTest extends LessTestBase {

  @Test
  public void testEquals() {
    Ruleset rulesetXY = ruleset(selector(element(".x"), element(CHILD, ".y")));
    rulesetXY.add(rule(prop("color"), color("black")));

    assertEquals(ruleset(selector(element(".x"))), ruleset(selector(element(".x"))));
    assertEquals(rulesetXY, rulesetXY);

    assertNotEquals(rulesetXY, null);
    assertNotEquals(rulesetXY, prop("foo"));
  }

  @Test
  public void testModelReprSafety() {
    Ruleset rulesetXY = ruleset(selector(element(".x"), element(CHILD, ".y")));
    rulesetXY.add(rule(prop("color"), color("black")));
    rulesetXY.toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.RULESET);

    Ruleset exp = ruleset(selector(element(Combinator.CHILD, ".x")));
    exp.add(rule(prop("a"), color("#fff")));
    h.parseEquals("> .x { a: #fff; }", exp);

    h.parseEquals(".foo { }", ruleset(selector(element(".foo"))));

    exp = ruleset(selector(element("span"), attr(null, anon("class"))));
    h.parseEquals("span[class] { }", exp);

    // Unicode character in selector
    exp = ruleset(selector(element(".♫")));
    exp.add(rule(prop("length"), dim(52, Unit.S)));
    h.parseEquals(".♫ { length: 52s; }", exp);
  }

}
