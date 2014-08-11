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

import static com.squarespace.less.ExecuteErrorType.VAR_CIRCULAR_REFERENCE;
import static com.squarespace.less.ExecuteErrorType.VAR_UNDEFINED;
import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.less.parse.Parselets.QUOTED;
import static com.squarespace.less.parse.Parselets.VARIABLE;
import static com.squarespace.less.parse.Parselets.VARIABLE_CURLY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Unit;


public class VariableTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(var("@a"), var("@a"));

    assertNotEquals(var("@a"), null);
    assertNotEquals(var("@a"), anon("@a"));
    assertNotEquals(var("@a"), var("@b"));
  }

  @Test
  public void testModelReprSafety() {
    var("@a").toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(VARIABLE);
    h.parseFails("@", INCOMPLETE_PARSE);
    h.parseFails("@-", INCOMPLETE_PARSE);
    h.parseEquals("@1", var("@1"));
    h.parseEquals("@a", var("@a"));
    h.parseEquals("@@a", var("@@a"));

    h = new LessHarness(VARIABLE_CURLY);
    h.parseFails("@{", INCOMPLETE_PARSE);
    h.parseFails("@{a", INCOMPLETE_PARSE);
    h.parseFails("@{aa", INCOMPLETE_PARSE);
    h.parseEquals("@{aa}", var("@aa", true));
  }

  @Test
  public void testUndefined() throws LessException {
    LessHarness h = new LessHarness();
    h.executeFails(".foo { color: @c; }", VAR_UNDEFINED);
  }

  @Test
  public void testSelfReference() throws LessException {
    GenericBlock defs = defs(
        def("@value", var("@link1")),
        def("@link1", var("@link2")),
        def("@link2", var("@value"))
        );
    LessHarness h = new LessHarness(VARIABLE, defs);
    h.evalFails("@value", VAR_CIRCULAR_REFERENCE);
    h.evalFails("@link1", VAR_CIRCULAR_REFERENCE);
    h.evalFails("@link2", VAR_CIRCULAR_REFERENCE);
  }

  @Test
  public void testIndirectReference() throws LessException {
    GenericBlock defs = defs(
        def("@value", dim(12, Unit.PX)),
        def("@real", quoted('"', false, var("@value"))),
        def("@one", kwd("real")),
        def("@two", quoted('"', true, "real"))
        );
    LessHarness h = new LessHarness(VARIABLE, defs);
    h.renderEquals("@@one", "\"12px\"");
    h.renderEquals("@@two", "\"12px\"");
  }

  @Test
  public void testDereference() throws LessException {
    GenericBlock defs = defs(
        def("@var", var("@link1")),
        def("@link1", var("@white")),
        def("@white", color("#fff")),
        def("@color", color("#123")),
        def("@num", dim(1, Unit.PX))
        );
    LessHarness h = new LessHarness(VARIABLE, defs);
    h.renderEquals("@var", "#fff");
    h.renderEquals("@color", "#123");
    h.renderEquals("@num", "1px");
  }

  @Test
  public void testEmbeddedReferences() throws LessException {
    GenericBlock defs = defs(
        def("@a", dim(1, Unit.EM)),
        def("@b", quoted('"', false, anon("foo"))),
        def("@c", dim(2, Unit.PX)),
        def("@d", quoted('"', false, anon("\"bar\""))),
        def("@e", color("#123456"))
        );
    LessHarness h = new LessHarness(QUOTED, defs);
    h.renderEquals("'@{a} @{b} @{c} @{d} @{e}", "'1em foo 2px \"bar\" #123456'");
  }

  @Test
  public void testNestedVariables() throws LessException {
    LessOptions opts = new LessOptions(true);
    LessHarness h = new LessHarness();

    String str = "@a: 1px; .x { @b: #111; .y { .z { key: @a @b; } } }";
    assertEquals(h.execute(str, opts), ".x .y .z{key:1px #111;}");

    str = "@a: 1; .x { @a: 2; .y { @a: 3; .z { key: @a; } } }";
    assertEquals(h.execute(str, opts), ".x .y .z{key:3;}");
  }

}
