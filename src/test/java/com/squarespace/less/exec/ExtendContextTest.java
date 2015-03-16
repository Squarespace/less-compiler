/**
 * Copyright, 2015, Squarespace, Inc.
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

package com.squarespace.less.exec;

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.model.Combinator.DESC;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Extend;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Selector;
import com.squarespace.less.parse.Parselets;


public class ExtendContextTest extends LessTestBase {

  @Test
  public void testBasicSearch() {

    ExtendList list = new ExtendList(false);

    Selector selector = selector(element(".a"), element(CHILD, ".b"));
    list.add(new Extend(selector, false));

    selector = selector(element(".a"), element(DESC, ".b"));
    list.add(new Extend(selector, false));

    selector = selector(element(".replace"));
    selector.extendList(list);

    ExtendContext context = new ExtendContext();
    context.index(selector);

    Selector query = selector(element(".a"), element(DESC, ".b"));
    List<Selector> result = context.resolve(query);
    assertEquals(result.size(), 1);
    assertEquals(result.get(0), selector(element(".replace")));
  }

  @Test
  public void testCombinatorPrefix() {
    ExtendList list = new ExtendList(false);

    Selector selector = selector(element(".a"), element(CHILD, ".b"));
    list.add(new Extend(selector, false));

    selector = selector(element(".replace"));
    selector.extendList(list);

    ExtendContext context = new ExtendContext();
    context.index(selector);

    Selector query = selector(element(null, ".a"), element(CHILD, ".b"));
    List<Selector> result = context.resolve(query);
    assertEquals(result.size(), 1);
    assertEquals(result.get(0), selector(element(".replace")));
  }

  @Test
  public void testPartialCombinatorPrefix() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    LessOptions opts = new LessOptions(true);

    String source = ".replace:extend( .b > .c all) {} .a > .b > .c { color: red; }";
    assertEquals(h.execute(source, opts), ".a>.b>.c,.a>.replace{color:red}");

    source = ".replace:extend(.b.c all) {} .a.b.c { color: blue; }";
    assertEquals(h.execute(source, opts), ".a.b.c,.a.replace{color:blue}");

    source = ".replace:extend(.a > .b + .c) {} .a > .b + .c { color: green; }";
    assertEquals(h.execute(source, opts), ".a>.b+.c,.replace{color:green}");
  }
}
