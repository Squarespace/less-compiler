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
import static com.squarespace.less.parse.Parselets.RULESET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Units;


public class RulesetTest extends LessTestBase {

  @Test
  public void testEquals() {
    Ruleset rulesetXY = ruleset(selector(element(".x"), comb(CHILD), element(".y")));
    rulesetXY.add(rule(prop("color"), color("black")));

    assertEquals(ruleset(selector(element(".x"))), ruleset(selector(element(".x"))));
    assertEquals(rulesetXY, rulesetXY);

    assertNotEquals(rulesetXY, null);
    assertNotEquals(rulesetXY, prop("foo"));
  }

  @Test
  public void testModelReprSafety() {
    Ruleset rulesetXY = ruleset(selector(element(".x"), comb(CHILD), element(".y")));
    rulesetXY.add(rule(prop("color"), color("black")));
    rulesetXY.toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(RULESET);

    Ruleset exp = ruleset(selector(comb(CHILD), element(".x")));
    exp.add(rule(prop("a"), color("#fff")));
    h.parseEquals("> .x { a: #fff; }", exp);

    h.parseEquals(".foo { }", ruleset(selector(element(".foo"))));

    exp = ruleset(selector(element("span"), attr(anon("class"))));
    h.parseEquals("span[class] { }", exp);

    // Unicode character in selector
    exp = ruleset(selector(element(".♫")));
    exp.add(rule(prop("length"), dim(52, Units.S)));
    h.parseEquals(".♫ { length: 52s; }", exp);
  }

  @Test
  public void testRepr() throws LessException {
    LessOptions opts = new LessOptions(true);
    LessHarness h = new LessHarness(RULESET);

    Buffer buf = new Buffer(0, true);
    String source = ".ruleset & & { color: red; }";
    Node tree = h.parse(source);
    tree.repr(buf);
    assertEquals(".ruleset & &{color:red}", buf.toString());

    String result = h.execute(source, opts);
    assertEquals(result, ".ruleset{color:red}");
  }


}
