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

import static com.squarespace.less.ExecuteErrorType.ARG_NAMED_NOTFOUND;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.MixinMatcher;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.LessSyntax;


public class ArgumentBinderTest extends LessTestBase {

  @Test
  public void testPatternMatch() throws LessException {

    // TESTS  assert..(parameters, arguments)

    // ARITY
    assertMatches("()", "()");
    assertMatches("(@a)", "(1)");
    assertMatches("(@a: 1)", "()");
    assertMatches("(@a: 1)", "(1)");
    assertMatches("(@a: 1, @b)", "(1)");

    assertMatchFails("()", "(1)");
    assertMatchFails("(@a)", "()");
    assertMatchFails("(@a)", "(1, 2)");

    // PATTERNS
    assertMatches("(true)", "(true)");
    assertMatches("(true, false)", "(true, false)");
    assertMatches("(1, 2)", "(1, 2)");
    assertMatches("(@a: 1, true)", "(2, true)");
    assertMatches("(@a: 1, foo)", "(2, foo)");
    assertMatches("(@a: 1, foo)", "(2; foo)");
    assertMatches("(true, @a: 1)", "(true)");
    assertMatches("(true, @a: 1)", "(true, 2)");
    assertMatches("(true, @a: 1)", "(true, @a: 2)");

    assertMatches("(@a: 1, true)", "(1)");

    assertMatchFails("(true)", "(1)");
    assertMatchFails("(@a: 1, false)", "(1, true)");
    assertMatchFails("(@a: 1, foo)", "(2; foo, bar)");
    assertMatchFails("(true, @a: 1)", "(@a: 1, true)");

    // VARIADIC
    assertMatches("(...)", "()");
    assertMatches("(@a ...)", "()");
    assertMatches("(...)", "(1, 2, 3)");
    assertMatches("(@a ...)", "(1, 2, 3)");
    assertMatches("(@a: 1, @b ...)", "()");
    assertMatches("(@a: 1, @b ...)", "(1)");
    assertMatches("(@a: 1, @b ...)", "(1, 2)");
    assertMatches("(@a: 1, @b ...)", "(1, 2, 3, 4, 5)");
    assertMatches("(3.14px, @a: 1, ...)", "(3.14px)");
    assertMatches("(3.14px, @a: 1, ...)", "(3.14px, 2)");
    assertMatches("(3.14px, @a: 1, ...)", "(3.14px, 2, 3, 4)");
    assertMatches("('foo')", "('foo')");

    assertMatchFails("(@a, @b ...)", "()");
    assertMatchFails("(false, @a: 1, @b ...)", "(true)");
    assertMatchFails("(false, @a: 1, @b ...)", "(@a: 2)");
    assertMatchFails("(3.14px, @a: 1, @b ...)", "(3.15px)");

    // EVAL + RENDER PATTERNS
    assertMatches("(~'foo')", "(foo)");
    assertMatches("(~'foo', ~'bar', ...)", "(foo, bar, 2, 3, 4)");
    assertMatches("(2)", "(1 + 1)");
    assertMatches("(#fff)", "(rgb(255, 255, 255))");

    assertMatchFails("(#fff)", "(rgb(32, 32, 32))");
  }

  @Test
  public void testFoo() throws LessException {
    assertMatchFails("(@a, @b ...)", "()");
  }

  @Test
  public void testBinding() throws LessException {
    assertBinds("(@a: 1)", "(2)", defs(

        def("@a", dim(2)),
        def("@arguments", expn(dim(2)))
        ));

    assertBinds("(@a: 'foo')", "(2)", defs(

        def("@a", dim(2)),
        def("@arguments", expn(dim(2)))
        ));

    assertBinds("(true, @a: 1)", "(true, 2px)", defs(

        def("@a", dim(2, Unit.PX)),
        def("@arguments", expn(dim(2, Unit.PX)))
        ));

    assertBinds("(...)", "(1, 2, 3)", defs(

        def("@arguments", expn(dim(1), dim(2), dim(3)))
        ));

    assertBinds("(@a: 1, @b ...)", "(12em, 1, 2, 3)", defs(

        def("@a", dim(12, Unit.EM)),
        def("@b", expn(dim(1), dim(2), dim(3))),
        def("@arguments", expn(dim(12, Unit.EM), dim(1), dim(2), dim(3)))
        ));

    assertBinds("(@b: 2, @a: 1)", "(@a: 3, @b: 4)", defs(

        def("@a", dim(3)),
        def("@b", dim(4)),
        def("@arguments", expn(dim(4), dim(3)))
        ));

    // BINDING FAILURES
    assertBindFails("(@b: 2)", "(@c: 1)", ARG_NAMED_NOTFOUND);

  }

  private Node parse(String raw, LessSyntax syntax) throws LessException {
    LessHarness h = new LessHarness(syntax);
    return h.parse(raw);
  }

//  private Node parse(String raw, Parselet[] parselets) throws LessException {
//    LessHarness h = new LessHarness(parselets);
//    return h.parse(raw);
//  }

  private void assertBinds(String rawParams, String rawArgs, GenericBlock defs) throws LessException {
    MixinParams params = (MixinParams) parse(rawParams, LessSyntax.MIXIN_PARAMS);
    MixinCallArgs args = (MixinCallArgs) parse(rawArgs, LessSyntax.MIXIN_CALL_ARGS);
    Map<String, Node> expected = new HashMap<>();
    FlexList<Node> rules = defs.block().rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Definition def = (Definition)rules.get(i);
      expected.put(def.name(), def.value());
    }

    GenericBlock result = bind(params, args);
    rules = result.block().rules();
    size = rules.size();
    for (int i = 0; i < size; i++) {
      Definition actual = (Definition)rules.get(i);
      Node value = expected.get(actual.name());
      assertEquals(actual.value(), value);
    }
  }

  private void assertBindFails(String rawParams, String rawArgs, LessErrorType errorType) throws LessException {
    MixinParams params = (MixinParams) parse(rawParams, LessSyntax.MIXIN_PARAMS);
    MixinCallArgs args = (MixinCallArgs) parse(rawArgs, LessSyntax.MIXIN_CALL_ARGS);
    try {
      bind(params, args);
      fail("Expected LessException of type " + errorType);
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), errorType);
    }
  }

  private GenericBlock bind(MixinParams params, MixinCallArgs args) throws LessException {
    LessHarness h = new LessHarness();
    ExecEnv env = h.context().newEnv();
    Selector sel = new Selector();
    MixinMatcher matcher = new MixinMatcher(env, new MixinCall(sel, args, false));
    return matcher.bind(params);
  }

  private void assertMatches(String rawParams, String rawArgs) throws LessException {
    MixinParams params = (MixinParams) parse(rawParams, LessSyntax.MIXIN_PARAMS);
    MixinCallArgs args = (MixinCallArgs) parse(rawArgs, LessSyntax.MIXIN_CALL_ARGS);
    assertTrue(patternMatch(params, args));
  }

  private void assertMatchFails(String rawParams, String rawArgs) throws LessException {
    MixinParams params = (MixinParams) parse(rawParams, LessSyntax.MIXIN_PARAMS);
    MixinCallArgs args = (MixinCallArgs) parse(rawArgs, LessSyntax.MIXIN_CALL_ARGS);
    assertFalse(patternMatch(params, args));
  }

  private boolean patternMatch(MixinParams params, MixinCallArgs args) throws LessException {
    LessHarness h = new LessHarness();
    ExecEnv env = h.context().newEnv();
    Selector sel = new Selector();
    MixinMatcher binder = new MixinMatcher(env, new MixinCall(sel, args, false));
    return binder.patternMatch(params);
  }

}
