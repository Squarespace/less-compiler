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

package com.squarespace.less;

import static com.squarespace.less.SyntaxErrorType.EXTEND_MISSING_TARGET;
import static com.squarespace.less.SyntaxErrorType.SELECTOR_END_EXTEND;
import static junit.framework.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.Parselets;


public class ExtendTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULESET);

    // Selector-level extend.
    String source = ".foo:extend(bar all baz all, .foo all, .bar .foo) { color: red; }";
    Node actual = h.parse(source);
    Buffer buf = new Buffer(2, true);
    actual.repr(buf);
    assertEquals(".foo:extend(bar all baz all,.foo all,.bar .foo){color:red}", buf.toString());

    // Rule level extend
    source = ".foo { &:extend(bar baz, baz all bar all); color: red; }";
    actual = h.parse(source);
    buf = new Buffer(2, true);
    actual.repr(buf);
    assertEquals(".foo{&:extend(bar baz,baz all bar all);color:red}", buf.toString());
  }

  @Test
  public void testParseFails() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULESET);

    h.parseFails(".foo:extend() { }", EXTEND_MISSING_TARGET);
    h.parseFails(".foo:extend(bar) baz { color: red; }", SELECTOR_END_EXTEND);
    h.parseFails(".baz .foo:extend(bar baz all) .bar { }", SELECTOR_END_EXTEND);
  }

  @Test
  public void testSelector() throws LessException {
    LessOptions opts = new LessOptions(true);
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    String result = h.execute("@name: ~'foo'; .@{name} { color: red; }", opts);
    assertEquals(".foo{color:red}", result);
  }

}
