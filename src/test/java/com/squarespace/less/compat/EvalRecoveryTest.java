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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.less.ExecuteErrorType;
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
  public void testSequentialRulesRecoverIndependently() throws LessException {
    // Two failing members in one block: each is dropped individually.
    String css = compile(".a { .bogus1(); .bogus2(); width: 3px; }", safeMode());
    assertTrue(css.contains("width: 3px"), css);
    assertTrue(css.contains("eval: dropped mixin call"), css);
  }
}
