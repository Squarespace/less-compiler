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

import static com.squarespace.less.SyntaxErrorType.SELECTOR_END_GUARD;
import static com.squarespace.less.SyntaxErrorType.SELECTOR_ONE_GUARD;
import static com.squarespace.less.model.CombinatorType.CHILD;
import static com.squarespace.less.model.CombinatorType.DESC;
import static com.squarespace.less.model.CombinatorType.SIB_ADJ;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.model.Selector;
import com.squarespace.less.parse.Parselets;


public class SelectorTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(selector(element(".x")), selector(element(".x")));
    assertEquals(selector(element(".x"), element(".y")), selector(element(".x"), element(".y")));

    assertNotEquals(selector(element(".x")), null);
    assertNotEquals(selector(element(".x")), prop("x"));
    assertNotEquals(selector(element(".x")), selector(element(".y")));
    assertNotEquals(selector(element(".x")), selector(comb(CHILD), element(".x")));
  }

  @Test
  public void testModelReprSafety() {
    selector(comb(CHILD), element(".foo"), element(".y")).toString();
  }

  @Test
  public void testRender() throws LessException {
    Selector sel = selector(element("li"), element(".bar"), comb(CHILD), element("span"), element(".foo"),
          comb(DESC), element("baz"));

    assertEquals(render(false, sel), "li.bar > span.foo baz");
    assertEquals(render(true, sel), "li.bar>span.foo baz");

    sel = selector(element("p"), element(".para"), comb(SIB_ADJ), element("span"), element(".word"),
        comb(DESC), element("b"));

    assertEquals(render(false, sel), "p.para + span.word b");
    assertEquals(render(true, sel), "p.para+span.word b");
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTOR);

    h.parseEquals("p", selector(element("p")));
    h.parseEquals(">p", selector(comb(CHILD), element("p")));
    h.parseEquals("> p", selector(comb(CHILD), element("p")));
    h.parseEquals("+ p.class", selector(comb(SIB_ADJ), element("p"), element(".class")));

    Selector exp = selector(element("p"), attr("class", "~=", quoted('"', false, "a")));
    h.parseEquals("p[class~=\"a\"]", exp);
  }

  @Test
  public void testParseGuard() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTORS);

    h.parseFails(".parent when not (@a = 1) foo bar ", SELECTOR_END_GUARD);
    h.parseFails(".parent when not (@a = 1), .child ", SELECTOR_ONE_GUARD);
  }

  private String render(boolean compress, Selector selector) throws LessException {
    LessContext ctx = new LessContext(new LessOptions(compress));
    ExecEnv env = ctx.newEnv();
    return env.context().render(selector);
  }

}
