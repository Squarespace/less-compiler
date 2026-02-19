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

package com.squarespace.less.compat;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.less.ExecuteErrorType;
import com.squarespace.less.HashMapLessLoader;
import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;


/**
 * Evaluation and render recovery in safe mode: failed block members and
 * mixin calls are dropped with a warning. Strict mode is unchanged.
 */
public class EvalRecoveryTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  private String compile(String raw, LessOptions opts) throws LessException {
    return COMPILER.compile(raw, new LessContext(opts));
  }

  private static LessOptions fixedSafeMode() {
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    opts.safeMode(true);
    return opts;
  }

  private static LessOptions safeMode() {
    LessOptions opts = new LessOptions();
    opts.safeMode(true);
    return opts;
  }

  private static LessOptions safeStrictContract() {
    LessOptions opts = new LessOptions();
    opts.safeMode(true);
    opts.strict(true); // division/mod contract: errors instead of warnings
    return opts;
  }

  @Test
  public void testUndefinedMixinCallStrictFails() {
    try {
      compile(".a { width: 1px; .bogus(); }", new LessOptions());
      fail("expected MIXIN_UNDEFINED");
    } catch (LessException e) {
      assertTrue(e.primaryError().type() == ExecuteErrorType.MIXIN_UNDEFINED, e.getMessage());
    }
  }

  @Test
  public void testUndefinedMixinCallRecovers() throws LessException {
    // The failing call is dropped. The sibling rule survives.
    String css = compile(".a { width: 1px; .bogus(); }", safeMode());
    assertTrue(css.contains("width: 1px"), css);
    assertTrue(css.contains("eval: dropped mixin call"), css);
  }

  @Test
  public void testDivByZeroRuleRecovers() throws LessException {
    // With the strict division contract, division by zero is a hard error.
    // Recovery drops just the failing rule.
    String css = compile(".a { width: (10px / 0); margin: 2px; }", safeStrictContract());
    assertTrue(!css.contains("width:"), css);
    assertTrue(css.contains("margin: 2px"), css);
    assertTrue(css.contains("eval: dropped rule"), css);
  }

  @Test
  public void testLegacyErrorsRecoverToo() throws LessException {
    // Best effort is orthogonal to the ladder: a legacy-mandated error at
    // the released level also recovers in safe mode.
    String css = compile(".m(@b...) { p: @b; }\n.x { .m(@b: 1); }", safeMode());
    assertTrue(css.contains("WARNING["), css);
  }

  @Test
  public void testSafeModeOverridePropagatesToEvaluation() throws LessException {
    // The 5-arg compile's safeMode boolean must drive evaluation too, not
    // just parsing: with default options and safeMode=true the mixin-call
    // error is recovered.
    LessContext ctx = new LessContext(new LessOptions());
    String css = COMPILER.compile(".a { width: 1px; .bogus(); }", ctx, null, null, true);
    assertTrue(css.contains("width: 1px"), css);
    assertTrue(css.contains("eval: dropped mixin call"), css);
  }

  @Test
  public void testDroppedMixinCallDoesNotBreakVariableLookup() throws LessException {
    // A dropped member leaves a null slot. A later splice resets the
    // variable cache and the lookup must not NPE in Block.buildVariables.
    String css = compile(".a {\n  .bogus();\n  .real();\n  y: @v;\n}\n.real { @v: 4px; p: 1; }\n",
        fixedSafeMode());
    assertTrue(css.contains("WARNING["), css);
    assertTrue(css.contains("4px") || css.contains(".real"), css);
  }

  @Test
  public void testDroppedRuleDoesNotBreakImportRendering() throws LessException {
    // A literal import sets FLAG_HAS_IMPORTS. A dropped rule leaves a
    // null slot that renderImports must skip.
    String css = compile("@import url(\"http://example.com/x.css\");\nx: (10px / 0);\n.a { color: red; }\n",
        fixedSafeMode());
    assertTrue(css.contains("WARNING["), css);
    assertTrue(css.contains(".a"), css);
  }

  @Test
  public void testSafeModeOverrideIsContextScoped() throws LessException {
    // The legacy boolean override must be transient and context-scoped: a
    // shared LessOptions used by a boolean-flag caller and a plain caller
    // must keep the plain caller strict.
    LessOptions shared = new LessOptions();
    LessContext ctxA = new LessContext(shared);
    ctxA.setCompiler(COMPILER);
    String css = COMPILER.compile(".a { width: 1px; .bogus(); }", ctxA, null, null, true);
    assertTrue(css.contains("WARNING["), css);   // ctxA recovered
    assertFalse(shared.safeMode(), "boolean caller mutated the shared options");

    LessContext ctxB = new LessContext(shared);
    ctxB.setCompiler(COMPILER);
    try {
      COMPILER.compile(".a { width: 1px; .bogus(); }", ctxB);
      fail("expected MIXIN_UNDEFINED; shared options poisoned by the boolean caller");
    } catch (LessException e) {
      assertTrue(e.primaryError().type() == ExecuteErrorType.MIXIN_UNDEFINED, e.getMessage());
    }
  }

  @Test
  public void testFailedMixinCallDoesNotLeakDepth() throws LessException {
    // A matched mixin whose body throws must unwind the mixin-depth
    // counter. With a leak, every later legit call chain trips the
    // recursion limit and is dropped.
    LessOptions opts = fixedSafeMode();
    opts.mixinRecursionLimit(3);
    String css = compile(
        ".m() { x: (1px / 0); }\n"
            + ".deep(@n) when (@n > 0) { .deep(@n - 1); }\n"
            + ".deep(@n) when (@n = 0) { p: done; }\n"
            + ".a { .m(); }\n"
            + ".b { .deep(2); }\n",
        opts);
    assertTrue(css.contains("p: done"), "legit call chain after a failed call was dropped:\n" + css);
  }

  @Test
  public void testFailedImportDoesNotLeakDepth() throws LessException {
    // An import hitting the recursion limit must unwind the import
    // depth. With a leak, every later import fails the depth check.
    Map<Path, String> files = new HashMap<>();
    files.put(Paths.get(".").resolve("a.less").toAbsolutePath().normalize(),
        "@import 'b.less';\n@import 'd.less';\n");
    files.put(Paths.get(".").resolve("b.less").toAbsolutePath().normalize(),
        "@import 'c.less';\n");
    files.put(Paths.get(".").resolve("c.less").toAbsolutePath().normalize(),
        "@import 'e.less';\n");
    files.put(Paths.get(".").resolve("e.less").toAbsolutePath().normalize(),
        ".e { color: black; }\n");
    files.put(Paths.get(".").resolve("d.less").toAbsolutePath().normalize(),
        ".d { color: green; }\n");

    LessOptions opts = fixedSafeMode();
    opts.importRecursionLimit(2);
    LessContext ctx = new LessContext(opts, new HashMapLessLoader(files));
    ctx.setCompiler(COMPILER);
    String css = COMPILER.compile("@import 'a.less';", ctx, Paths.get("."), Paths.get("t.less"));
    // The recursive chain (a->b->c->e) trips the limit and is dropped.
    // The following independent import must still resolve.
    assertTrue(css.contains(".d"), "later import dropped after a limit failure:\n" + css);
    assertTrue(!css.contains(".e"), css);
  }

  @Test
  public void testDuplicateRecoveryWarningsAreDeduped() throws LessException {
    // A mixin-defining ruleset is evaluated once as output and once as
    // a mixin expansion. Each evaluation drops the same rule. The warning
    // ledger must emit one entry for the source line, not two.
    String css = compile(".m { x: (1px / 0); y: 2px; }\n.a { .m(); }\n", fixedSafeMode());
    assertEquals(css.split("DIVIDE_BY_ZERO", -1).length - 1, 1, css);
    assertTrue(css.contains("y: 2px"), css);
  }

  @Test
  public void testSequentialRulesRecoverIndependently() throws LessException {
    // Two failing members in one block: each is dropped individually.
    String css = compile(".a { .bogus1(); .bogus2(); width: 3px; }", safeMode());
    assertTrue(css.contains("width: 3px"), css);
    assertTrue(css.contains("eval: dropped mixin call"), css);
  }
}
