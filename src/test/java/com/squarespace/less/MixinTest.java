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

import static com.squarespace.less.core.Constants.TRUE;
import static com.squarespace.less.ExecuteErrorType.DIVIDE_BY_ZERO;
import static com.squarespace.less.ExecuteErrorType.VAR_UNDEFINED;
import static com.squarespace.less.model.Operator.EQUAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;


public class MixinTest extends LessTestBase {

  @Test
  public void testEquals() {
    MixinParams params = params(param("@a"), param("@b", anon("c")));
    Guard guard = guard(cond(EQUAL, var("@a"), anon("b")));

    assertEquals(mixin(".foo"), mixin(".foo"));
    assertEquals(mixin(".foo", params, guard), mixin(".foo", params, guard));

    assertNotEquals(mixin(".foo"), null);
    assertNotEquals(mixin(".foo"), mixin(".bar"));
    assertNotEquals(mixin(".foo"), mixin(".foo", params, guard));
    assertNotEquals(mixin(".foo", params, guard), mixin(".foo"));
    assertNotEquals(mixin(".foo", params, null), mixin(".foo", params, guard));
  }

  @Test
  public void testModelReprSafety() {
    MixinParams params = params(param("@a"), param("@b", anon("c")));
    Guard guard = guard(cond(EQUAL, var("@a"), anon("b")));

    mixin(".foo").toString();
    mixin("#ns", params, guard).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness();

    Stylesheet exp = stylesheet();
    Node mixin = mixin(".x", params(param("@a")), guard(cond(EQUAL, var("@b"), TRUE, true)));
    exp.add(mixin);

    h.parseEquals(".x(@a) when not (@b) { }", exp);
  }


  @Test
  public void testEval() {
    String source = ".mixin-1() {\n"
        + "  .foo {\n"
        + "    color: red;\n"
        + "    .mixin-2();\n"
        + "  }\n"
        + "}\n"
        + "\n"
        + ".mixin-2() {\n"
        + "  .bar {\n"
        + "    color: blue;\n"
        + "    .mixin-1();\n"
        + "  }\n"
        + "}\n"
        + "\n"
        + ".parent {\n"
        + "  .mixin-1();\n"
        + "}";
    LessOptions opts = new LessOptions();
    opts.mixinRecursionLimit(2);
    LessCompiler compiler = new LessCompiler();
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(compiler);
    try {
      compiler.compile(source, ctx);
      fail("compile should fail with a mixin recursion error");
    } catch (LessException e) {
      // expected
    }
  }

  /**
   * Depth counters must not leak between compiles on a reused context.
   * Repeated failed compiles used to poison the next healthy compile.
   */
  @Test
  public void testMixinDepthResetOnReusedContext() throws LessException {
    String recursive = ".mixin-1() { .mixin-1(); } .parent { .mixin-1(); }";
    String healthy = ".mixin-1() { .foo { color: red; } } .parent { .mixin-1(); }";

    LessOptions opts = new LessOptions();
    opts.mixinRecursionLimit(3);
    LessCompiler compiler = new LessCompiler();
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(compiler);

    // Fail several times, then a healthy compile must still succeed.
    for (int i = 0; i < 3; i++) {
      try {
        compiler.compile(recursive, ctx);
        fail("compile should fail with a mixin recursion error");
      } catch (LessException e) {
        // expected
      }
    }

    assertEquals(compiler.compile(healthy, ctx), ".parent .foo {\n  color: red;\n}\n");
  }

  @Test
  public void testMixinBodyErrorPropagates() {
    // An error raised inside a mixin body must fail the compile, not be
    // silently swallowed along with a partially expanded body.
    String source = ".m() { color: red; color: @undefvar; } .a { .m(); }";
    LessCompiler compiler = new LessCompiler();
    LessContext ctx = new LessContext();
    ctx.setCompiler(compiler);
    try {
      compiler.compile(source, ctx);
      fail("compile should fail with the error raised in the mixin body");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), VAR_UNDEFINED);
    }
  }

  @Test
  public void testMixinBodyWarningsPreserved() throws LessException {
    // A warning raised while evaluating a mixin body must be attached to
    // the produced rule, exactly like the same rule outside a mixin.
    LessHarness h = new LessHarness();
    LessOptions opts = new LessOptions();
    opts.strict(false);
    String withMixin = h.execute(".m(){width:1px/0;} .a{.m();}", opts);
    String plain = h.execute(".a{width:1px/0;}", opts);
    assertEquals(withMixin, plain);
    assertTrue(withMixin.contains("WARNING["), withMixin);
  }

  @Test
  public void testMixinBodyColorMathWarningPreserved() throws LessException {
    // An incompatible-units warning (2px + blue) raised for a rule
    // inside a mixin body must render like the identical inline rule.
    LessHarness h = new LessHarness();
    LessOptions opts = new LessOptions();
    opts.strict(false);
    String withMixin = h.execute(".m(){a:2px + blue;} .x{.m();}", opts);
    String plain = h.execute(".x{a:2px + blue;}", opts);
    assertEquals(withMixin, plain);
    assertTrue(withMixin.contains("WARNING["), withMixin);
  }

  @Test
  public void testMixinBodyWarningsSurviveNestedExpansion() throws LessException {
    // Warnings must survive nested mixin calls, multi-value expressions
    // and important calls, matching the inline rule byte for byte.
    LessHarness h = new LessHarness();
    LessOptions opts = new LessOptions();
    opts.strict(false);
    assertEquals(
        h.execute(".m(){margin:2px + blue 1px;} .n(){.m();} .x{.n() !important;}", opts),
        h.execute(".x{margin:2px + blue 1px !important;}", opts));
    assertTrue(
        h.execute(".m(){margin:2px + blue 1px;} .n(){.m();} .x{.n();}", opts)
            .contains("WARNING["));
  }

  @Test
  public void testMixinBodyFunctionWarningPreserved() throws LessException {
    // An explicit env.addWarning() from a function (replace() is flagged
    // experimental) attached in a mixin body must survive expansion.
    LessHarness h = new LessHarness();
    LessOptions opts = new LessOptions();
    opts.strict(false);
    String withMixin = h.execute(".m(){a:replace('abc','b','c');} .x{.m();}", opts);
    String plain = h.execute(".x{a:replace('abc','b','c');}", opts);
    assertEquals(withMixin, plain);
    assertTrue(withMixin.contains("WARNING["), withMixin);
  }

  @Test
  public void testMixinBodyShadowedErrorPropagates() {
    // The call argument (0) must shadow the root-scope @x:1px inside the
    // mixin: the strict divide-by-zero fails the compile instead of
    // silently emitting width:1px from the root value.
    LessHarness h = new LessHarness();
    LessOptions opts = new LessOptions();
    opts.strict(true);
    try {
      h.execute(".m(@x){width:1px/@x;} @x:1px; .a{.m(0);}", opts);
      fail("compile should fail with a divide-by-zero error");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), DIVIDE_BY_ZERO);
    }
  }

  @Test
  public void testMixinBodyErrorContextHasCall() {
    // The error context must include the mixin call with its actual
    // arguments, so the failure is attributed to the call site.
    LessHarness h = new LessHarness();
    try {
      h.execute(".m(@x){width:1px/@x;} @x:1px; .a{.m(0);}");
      fail("compile should fail with a divide-by-zero error");
    } catch (LessException e) {
      boolean hasCall = false;
      for (Node node : e.errorContext()) {
        if (node instanceof MixinCall && ((MixinCall)node).args() != null) {
          hasCall = true;
        }
      }
      assertTrue(hasCall, e.errorContext().toString());
    }
  }
}
