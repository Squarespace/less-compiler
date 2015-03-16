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
import static com.squarespace.less.model.CombinatorType.DESC;
import static com.squarespace.less.model.CombinatorType.SIB_ADJ;
import static com.squarespace.less.model.CombinatorType.SIB_GEN;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Unit;


public class ValueRendererTest extends LessTestBase {

  @Test
  public void testComment() throws LessException {
    assertEquals(render(comment(" foo * // * bar ", true)), "/* foo * // * bar */");
    assertEquals(render(comment(" bar /* x */ baz", false)), "// bar /* x */ baz");
  }

  @Test
  public void testDimension() throws LessException {
    assertEquals(render(dim(12, Unit.PX)), "12px");
    assertEquals(render(dim(3.14, Unit.IN)), "3.14in");
    assertEquals(render(dim(2.1819)), "2.1819");
  }

  @Test
  public void testParenthesis() throws LessException {
    assertEquals(render(paren(kwd("foo"))), "(foo)");
    assertEquals(render(paren(dim(12, Unit.CM))), "(12cm)");
  }

  @Test
  public void testQuoted() throws LessException {
    assertEquals(render(quoted('"', false, "foo", "\"bar")), "\"foo\"bar\"");
    assertEquals(render(quoted('\'', false, "bar")), "'bar'");
    assertEquals(render(quoted('"', true, "bar", "foo")), "barfoo");
  }

  @Test
  public void testSelector() throws LessException {
    List<Selector> selectors = Arrays.asList(
        selector(comb(CHILD), element("span"), comb(DESC), element("b")),
        selector(element("p"), comb(SIB_ADJ), element("div"), comb(CHILD), element("span")),
        selector(element("p"), element(".class-1"), element(".class-2")),
        selector(
              element("ul"), comb(DESC), element("li"),
              element(".one"), comb(SIB_GEN), element("li"), element(".two")),
        selector(element("a"), element(".b"), comb(DESC), element("c"), comb(CHILD), element("d"))
        );

    List<String> normal = Arrays.asList(
        "> span b",
        "p + div > span",
        "p.class-1.class-2",
        "ul li.one ~ li.two",
        "a.b c > d"
        );

    List<String> compressed = Arrays.asList(
        ">span b",
        "p+div>span",
        "p.class-1.class-2",
        "ul li.one~li.two",
        "a.b c>d"
        );
    for (int i = 0; i < selectors.size(); i++) {
      assertEquals(render(selectors.get(i)), normal.get(i));
      assertEquals(compress(selectors.get(i)), compressed.get(i));
    }
  }

  private String render(Node node) throws LessException {
    return render(node, false);
  }

  private String compress(Node node) throws LessException {
    return render(node, true);
  }

  private String render(Node node, boolean compress) throws LessException {
    LessContext ctx = new LessContext(new LessOptions(compress));
    ExecEnv env = ctx.newEnv();
    return env.context().render(node);
  }



}