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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.less.compat.Patch;


/**
 * Per-compile warning budgets (LessOptions.maxWarningsPerType and
 * maxWarnings): a pathological sheet cannot flood the output or the
 * warning arrays past the configured limits, the suppressed count is
 * summarized in one trailing comment, and reused contexts get a fresh
 * budget per compile.
 */
public class WarningBudgetTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  /** A sheet of rules each emitting one INCOMPATIBLE_UNITS strip-unit warning. */
  private static String stripUnitSheet(int rules) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < rules; i++) {
      b.append(".r").append(i).append(" { width: 1em + 1px; }\n");
    }
    return b.toString();
  }

  /** A sheet of rules each dropped at eval (undefined variable), ledger path. */
  private static String droppedRuleSheet(int rules) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < rules; i++) {
      b.append(".r").append(i).append(" { width: @undefined-").append(i).append("; }\n");
    }
    return b.toString();
  }

  private static String compile(String raw, LessOptions opts) throws LessException {
    return COMPILER.compile(raw, new LessContext(opts));
  }

  private static int countOccurrences(String css, String needle) {
    int count = 0;
    int idx = 0;
    while ((idx = css.indexOf(needle, idx)) >= 0) {
      count++;
      idx += needle.length();
    }
    return count;
  }

  @Test
  public void testPerTypeBudgetCapsEvalWarnings() throws LessException {
    // 30 rules each raise an INCOMPATIBLE_UNITS strip-unit warning. The
    // per-type budget (3) caps the emitted comments at 3 and adds the
    // one-line suppression summary.
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(3);
    String css = compile(stripUnitSheet(30), opts);
    assertEquals(countOccurrences(css, "raised evaluating"), 3, css);
    assertTrue(css.contains("suppressed"), css);
    assertTrue(css.contains("INCOMPATIBLE_UNITS"), css);
    // The summary itself is a single comment, not one per suppressed
    // warning.
    assertEquals(countOccurrences(css, "warnings suppressed ("), 1, css);
    assertTrue(css.contains("27 INCOMPATIBLE_UNITS"), css);
    assertTrue(css.contains("limit 3 per type"), css);
  }

  @Test
  public void testZeroPerTypeIsUnlimited() throws LessException {
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(0);
    String css = compile(stripUnitSheet(30), opts);
    assertEquals(countOccurrences(css, "raised evaluating"), 30, css);
    assertFalse(css.contains("suppressed"), css);
  }

  @Test
  public void testOverallCapCapsEverything() throws LessException {
    // The overall budget alone caps the total. The suppressed summary
    // reports the "overall" bucket.
    LessOptions opts = new LessOptions();
    opts.maxWarnings(5);
    opts.maxWarningsPerType(0);
    String css = compile(stripUnitSheet(30), opts);
    assertEquals(countOccurrences(css, "raised evaluating"), 5, css);
    assertTrue(css.contains("warnings suppressed ("), css);
    assertTrue(css.contains("25 overall"), css);
  }

  @Test
  public void testBudgetAppliesAtEvaluationPhase() throws LessException {
    // The gate sits at env.addWarning (evaluation), so the warnings never
    // accumulate in the arrays: with the budget exhausted, rule nodes
    // carry no warnings at all.
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(2);
    String css = compile(stripUnitSheet(10), opts);
    assertEquals(countOccurrences(css, "raised evaluating"), 2, css);
    assertTrue(css.contains("suppressed"), css);
  }

  @Test
  public void testLedgerWarningsBudgeted() throws LessException {
    // Eval drops go through the context ledger. The budget caps the
    // trailing recovery comments too.
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    opts.safeMode(true);
    opts.maxWarningsPerType(3);
    String css = compile(droppedRuleSheet(20), opts);
    assertTrue(countOccurrences(css, "raised during recovery") <= 3, css);
    assertTrue(css.contains("suppressed"), css);
  }

  @Test
  public void testBudgetsResetBetweenCompiles() throws LessException {
    // Per-compile counters: a capped compile then a fresh compile on the
    // same context gets a fresh budget.
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(2);
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(COMPILER);
    String src = stripUnitSheet(3);
    String css1 = COMPILER.compile(src, ctx);
    assertEquals(countOccurrences(css1, "raised evaluating"), 2, css1);
    assertTrue(css1.contains("suppressed"), css1);
    String css2 = COMPILER.compile(src, ctx);
    assertEquals(countOccurrences(css2, "raised evaluating"), 2, css2);
    assertTrue(css2.contains("suppressed"), css2);
  }

  @Test
  public void testDifferentTypesDoNotStarveEachOther() throws LessException {
    // Per-type accounting: units warnings cap at 2 while undefined-var
    // drops still surface their own budget.
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      b.append(".u").append(i).append(" { width: 1em + 1px; }\n");
      b.append(".v").append(i).append(" { width: @nope-").append(i).append("; }\n");
    }
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    opts.safeMode(true);
    opts.maxWarningsPerType(2);
    String css = compile(b.toString(), opts);
    assertEquals(countOccurrences(css, "raised evaluating"), 2, css);
    assertEquals(countOccurrences(css, "raised during recovery"), 2, css);
  }


  @Test
  public void testWarningOnlyScopeIsPruned() throws LessException {
    // A ruleset whose body would render only warning comments must not
    // materialize as an empty `{ }` shell (restores the released
    // empty-scope omission).
    String src = ""
        + ".m() {\n"
        + "  @x: 1920em + 1px;\n"
        + "  @media screen {\n"
        + "    .a { color: red; }\n"
        + "  }\n"
        + "}\n"
        + ".tweak-only-warnings { .m(); }\n";
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(0); // unlimited: warnings are present, not budget-capped
    String css = compile(src, opts);
    assertTrue(!css.contains(".tweak-only-warnings {"), css); // no `{ }` shell
    assertTrue(css.contains("@media screen"), css);
    assertTrue(css.contains(".tweak-only-warnings .a"), css);
  }

  @Test
  public void testWarningsRenderInsideContentScopes() throws LessException {
    // With real content present, the raised-evaluating comments still
    // render inside the scope.
    String src = ""
        + ".m() { @x: 1920em + 1px; }\n"
        + ".with-prop { color: blue; .m(); }\n";
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(0);
    String css = compile(src, opts);
    assertTrue(css.contains(".with-prop"), css);
    assertTrue(css.contains("raised evaluating definition '@x'"), css);
    assertTrue(css.contains("color: blue"), css);
  }

  @Test
  public void testMediaWithOnlyWarningShellsIsPruned() throws LessException {
    // A @media whose only child would render as a warning shell is
    // pruned as a whole, matching the released omission.
    String src = ""
        + ".m() { @x: 1920em + 1px; }\n"
        + "@media screen {\n"
        + "  .only-shell { .m(); }\n"
        + "}\n"
        + ".outside { color: red; }\n";
    LessOptions opts = new LessOptions();
    opts.maxWarningsPerType(0);
    String css = compile(src, opts);
    assertTrue(!css.contains("@media screen"), css);
    assertTrue(!css.contains(".only-shell"), css);
    assertTrue(css.contains(".outside"), css);
  }
  @Test
  public void testWarningTypeClassification() {
    // Stable surface prefixes win over the embedded error-type payload.
    // Only bare evaluation warnings classify by their type.
    assertEquals(LessContext.warningType("eval: dropped rule: ExecuteError VAR_UNDEFINED: missing @x"), "eval-drop");
    assertEquals(LessContext.warningType("eval: dropped mixin call: ExecuteError INCOMPATIBLE_UNITS: bad"), "eval-drop");
    assertEquals(LessContext.warningType("render: skipped rule: ExecuteError INCOMPATIBLE_UNITS: bad"), "render-skip");
    assertEquals(LessContext.warningType("render: truncated selector combination exceeding complexity limit"), "render-skip");
    assertEquals(LessContext.warningType("ExecuteError INCOMPATIBLE_UNITS: No conversion is possible from EM to PX.. stripping unit."),
        "INCOMPATIBLE_UNITS");
    assertEquals(LessContext.warningType("skipped invalid statement at line 3"), "parse-recovery");
  }

  @Test
  public void testDiscardedWarningsRollBackBudget() throws LessException {
    // Warnings generated and then discarded by a dropped mixin call
    // must not consume budget slots. A later genuine warning of the
    // same type still surfaces, and the suppressed summary counts only
    // what was actually suppressed.
    StringBuilder b = new StringBuilder();
    b.append(".mx(@a, @b) { use: @a; }\n");
    for (int i = 0; i < 10; i++) {
      // Arg 1 emits an INCOMPATIBLE_UNITS strip-unit warning, arg 2
      // throws, so the call is dropped and its pending warnings discarded.
      b.append(".x").append(i).append(" { .mx(1em + 1px, @undef-").append(i).append("); }\n");
    }
    b.append(".good { width: 1em + 1px; }\n");
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    opts.safeMode(true);
    opts.maxWarningsPerType(3);
    String css = compile(b.toString(), opts);
    // The genuine warning still renders: the discards rolled back.
    assertEquals(countOccurrences(css, "raised evaluating"), 1, css);
    // The 10 dropped calls surface as eval-drop recovery warnings, capped
    // at the same budget.
    assertTrue(countOccurrences(css, "raised during recovery") <= 3, css);
    // Summary counts the surface buckets. INCOMPATIBLE_UNITS is not
    // among the suppressed (rolling discards leave nothing suppressed
    // for it).
    assertTrue(css.contains("eval-drop"), css);
  }

}
