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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.model.Combinator;
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
    assertNotEquals(selector(element(DESC, ".x")), selector(element(CHILD, ".x")));
    assertNotEquals(selector(element(null, ".x")), selector(element(CHILD, ".x")));
  }

  @Test
  public void testModelReprSafety() {
    selector(element(CHILD, ".foo"), element(".y")).toString();
  }

  @Test
  public void testRender() throws LessException {
    Selector sel = selector(element("li"), element(null, ".bar"), element(CHILD, "span"), element(null, ".foo"),
          element(DESC, "baz"));

    assertEquals(render(false, sel), "li.bar > span.foo baz");
    assertEquals(render(true, sel), "li.bar>span.foo baz");

    sel = selector(element("p"), element(null, ".para"), element(SIB_ADJ, "span"), element(null, ".word"),
        element(DESC, "b"));

    assertEquals(render(false, sel), "p.para + span.word b");
    assertEquals(render(true, sel), "p.para+span.word b");
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTOR);

    h.parseEquals("p", selector(element(DESC, "p")));
    h.parseEquals("> p", selector(element(Combinator.CHILD, "p")));
    h.parseEquals("+ p.class", selector(element(Combinator.SIB_ADJ, "p"), element(null, ".class")));

    Selector exp = selector(element("p"), attr(null, "class", "~=", quoted('"', false, "a")));
    h.parseEquals("p[class~=\"a\"]", exp);
  }

  @Test
  public void testWildcardNoAncestors() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    LessOptions opts = new LessOptions(true);
    assertEquals(h.execute(".ruleset & & & { color: red; }"), ".ruleset {\n  color: red;\n}\n");
    assertEquals(h.execute(".ruleset & & & { color: red; }", opts), ".ruleset{color:red}");
  }

  private String render(boolean compress, Selector selector) throws LessException {
    LessContext ctx = new LessContext(new LessOptions(compress));
    ExecEnv env = ctx.newEnv();
    return env.context().render(selector);
  }

}
