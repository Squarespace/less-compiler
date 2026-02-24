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

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;


/**
 * Parser recovery in safe mode: hard errors become warn-and-continue at
 * synchronization points. Strict mode is unchanged. The per-cell
 * outcomes are pinned by RecoveryMatrixTest.
 */
public class RecoveryModeTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  private static final String MEDIA_BLOCKLESS =
      "@media only screen and (max-width: 640px)\n#content {\n  padding-top: 50px;\n}\n";

  private static final String VAR_PAREN =
      "@dk-gray: #333;\n.dark-bg {\n  background-color: @dk-gray();\n}\n";

  private static final String INVALID_ADDITION =
      "@foo: 10px;\n.a {\n  width: @foo + px;\n}\n";

  private String compile(String raw, LessOptions opts) throws LessException {
    return COMPILER.compile(raw, new LessContext(opts));
  }

  private static LessOptions fixedSafe() {
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    opts.safeMode(true);
    return opts;
  }

  private static LessOptions fixedStrict() {
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    opts.safeMode(false);
    return opts;
  }

  private static LessOptions releasedSafe() {
    LessOptions opts = new LessOptions();
    opts.compatLevel(0);
    opts.safeMode(true);
    return opts;
  }

  private static void assertRecovered(String css) {
    assertTrue(css.contains("WARNING["), "expected a recovery warning, css:\n" + css);
  }

  private static void assertRecoveredWith(String css, String text) {
    assertTrue(css.contains(text), "expected warning containing '" + text + "', css:\n" + css);
  }

  @Test
  public void testBlocklessMediaRecovers() throws LessException {
    // BUG2 at the fixed level: the directive is dropped, the following
    // ruleset survives the block-aware sync (matching the released
    // DUMMY_MEDIA output), and the file compiles with a warning.
    String css = compile(MEDIA_BLOCKLESS, fixedSafe());
    assertTrue(css.contains("#content"), css);
    assertTrue(css.contains("padding-top: 50px"), css);
    assertRecoveredWith(css, "skipped invalid statement");
  }

  @Test
  public void testSelectorTruncationCapsCombinedSet() throws LessException {
    // The complexity budget spans every current selector of a
    // ruleset. Two selectors each individually under the threshold but
    // 4680 elements combined must truncate to the ~4096-element bound
    // (~114 rendered combos), not render all ~226.
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < 65; i++) {
      if (i > 0) {
        b.append(", ");
      }
      b.append(".a").append(i);
    }
    b.append(" {\n");
    for (int d = 0; d < 34; d++) {
      b.append("  ").append(".b").append(d).append(" {\n");
    }
    b.append("  .z1, .z2 {\n");
    b.append("    color: red;\n");
    b.append("  }\n");
    for (int d = 33; d >= 0; d--) {
      b.append("  }");
    }
    b.append("}\n");

    String css = compile(b.toString(), fixedSafe());
    int combos = css.split("\\.b33", -1).length - 1;
    assertTrue(combos > 0, css);
    // 4096 elements / 36 per combo = 113. The per-call cap would allow
    // up to ~226. Pin the global bound.
    assertTrue(combos <= 130, "rendered " + combos + " combos, expected <= 130:\n" + css);
    assertTrue(css.contains("raised during recovery"), css);
  }

  @Test
  public void testTopLevelErrorKeepsFollowingRulesets() throws LessException {
    // A stray bad line between two valid rulesets must not truncate
    // the rest of the file. The followers re-parse at their line starts.
    String css = compile(".a { color: red; }\n!!!\n.b { color: blue; }\n.c { font-size: 12px; }\n",
        fixedSafe());
    assertTrue(css.contains(".a"), css);
    assertTrue(css.contains(".b"), css);
    assertTrue(css.contains("font-size: 12px"), css);
    assertTrue(!css.contains("truncated"), css);
    assertRecovered(css);
  }

  @Test
  public void testTopLevelErrorKeepsFollowingDeclarations() throws LessException {
    // The first ';' terminates the NEXT statement. Recovery resumes
    // at that statement's line start so it survives.
    String css = compile("!!!\ny: 1;\n###\nz: 2;\n", fixedSafe());
    assertTrue(css.contains("y: 1"), css);
    assertTrue(css.contains("z: 2"), css);
  }

  @Test
  public void testMultiLineStringAfterErrorKeepsValidStatements() throws LessException {
    // Statement boundaries tracked during recovery are string-aware.
    // The bare-LF string is itself invalid (QUOTED_BARE_LF, a released
    // hard error), but the valid statement after it must survive instead
    // of being swallowed by the phantom-string cascade.
    String css = compile("!!!\nx: \"abc\ndef\";\ny: 1;\n", fixedSafe());
    assertTrue(css.contains("y: 1"), css);
    assertTrue(!css.contains("truncated"), css);
  }

  @Test
  public void testMultiLineStringAfterErrorInBlock() throws LessException {
    // Same shape inside a block: the ruleset survives with its valid
    // declaration. No unclosed-block truncation.
    String css = compile(".a {\n  !!!\n  x: \"abc\n  def\";\n  y: 1;\n}\n", fixedSafe());
    assertTrue(css.contains(".a"), css);
    assertTrue(css.contains("y: 1"), css);
    assertTrue(!css.contains("truncated"), css);
    assertTrue(!css.contains("unclosed"), css);
  }

  @Test
  public void testMultiLineCommentAfterErrorKeepsValidStatements() throws LessException {
    // The forward boundary tracker skips newlines inside block comments.
    String css = compile("!!!\n/* multi\nline */\nx: 1;\ny: 2;\n", fixedSafe());
    assertTrue(css.contains("x: 1"), css);
    assertTrue(css.contains("y: 2"), css);
  }

  @Test
  public void testMultiLineStatementAfterErrorSurvives() throws LessException {
    // A newline mid-statement is not a statement boundary. Recovery
    // resumes at the statement's start and the parser decides.
    String css = compile("!!!\nx: a\nb;\n", fixedSafe());
    assertTrue(css.contains("x: a b"), css);
  }

  @Test
  public void testMultiLineSelectorListAfterErrorKeepsAllSelectors() throws LessException {
    // Dropping one line of a multi-line selector list used to render
    // silently wrong CSS for the dropped selector.
    String css = compile("!!!\n.a,\n.b { color: red; }\n", fixedSafe());
    assertTrue(css.contains(".a,"), css);
    assertTrue(css.contains(".b"), css);
    assertTrue(css.contains("color: red"), css);
  }

  @Test
  public void testNewlineTerminatedDeclarationAfterErrorSurvives() throws LessException {
    String css = compile("!!!\nx: 1\ny: 2;\n", fixedSafe());
    assertTrue(css.contains("y: 2"), css);
  }

  @Test
  public void testLineCommentMidStatementDoesNotMoveBoundary() throws LessException {
    // A line comment inside a multi-line statement must not move the
    // boundary. The whole declaration re-parses and survives.
    String css = compile("!!!\nx: a // c\nb;\n", fixedSafe());
    assertTrue(css.contains("x: a"), css);
    assertTrue(!css.contains("produced no output"), css);
  }

  @Test
  public void testLineCommentMidSelectorListKeepsAllSelectors() throws LessException {
    String css = compile("!!!\n.a, // c\n.b { color: red; }\n", fixedSafe());
    assertTrue(css.contains(".a,"), css);
    assertTrue(css.contains(".b"), css);
    assertTrue(css.contains("color: red"), css);
  }

  @Test
  public void testGarbageWithTrailingLineCommentRecovers() throws LessException {
    // The boundary the comment-newline rule exists for: the garbage line
    // ends in a line comment, and the next statement must survive.
    String css = compile("!!! // c\ny: 1;\n", fixedSafe());
    assertTrue(css.contains("y: 1"), css);
    assertTrue(!css.contains("produced no output"), css);
  }

  @Test
  public void testEscapedLfStringAfterErrorSurvives() throws LessException {
    // A backslash-escaped line feed continues a valid multi-line string.
    // The scanner must not register a boundary or a phantom-string state
    // across it.
    String css = compile("!!!\nx: \"abc\\\ndef\";\ny: 1;\n", fixedSafe());
    assertTrue(css.contains("y: 1"), css);
    assertTrue(css.contains("abc\\" + "\n" + "def"), css);
  }

  @Test
  public void testRecoveryDoesNotLoopOnGarbageBlocks() throws LessException {
    // A garbage selector with a block is dropped. The surrounding
    // rulesets survive and recovery terminates.
    String css = compile(".a { color: red; }\n### { color: blue; }\n.z { font-size: 12px; }\n",
        fixedSafe());
    assertTrue(css.contains(".a"), css);
    assertTrue(css.contains(".z"), css);
    assertTrue(!css.contains("###"), css);
  }

  @Test
  public void testVarParenRecovers() throws LessException {
    // BUG3 at the fixed level: the failing declaration is dropped inside
    // the block (the empty ruleset is omitted from the output).
    String css = compile(VAR_PAREN, fixedSafe());
    assertRecoveredWith(css, "skipped invalid statement at line 3");
  }

  @Test
  public void testInvalidAdditionRecovers() throws LessException {
    // BUG4 at the fixed level.
    String css = compile(INVALID_ADDITION, fixedSafe());
    assertRecovered(css);
  }

  @Test
  public void testGarbageInputIsHardError() {
    // Recovery that rescues nothing is a broken sheet. A hard error even
    // in safe mode, so a blank stylesheet can never ship on a green build
    // (that is 1.7.2's failure contract for the worst input class. The
    // partially-broken sheets recovery is meant to save still compiles).
    try {
      compile("!!!\ngarbage\n###", fixedSafe());
      fail("expected empty-recovery compile to fail");
    } catch (LessException e) {
      assertTrue(e.getMessage().contains("produced no output"), e.getMessage());
    }
  }

  @Test
  public void testEmptyInputStillValid() throws LessException {
    // Parity guard: genuinely empty (or comment-only) input is not a
    // recovery outcome, so it compiles to empty output without warnings,
    // exactly like 1.7.2's CLI (empty file: exit 0, empty css).
    String css = compile("", fixedSafe());
    assertTrue(!css.contains("WARNING["), css);
    css = compile("/* just a comment */", fixedSafe());
    assertTrue(css.contains("just a comment"), css);
    assertTrue(!css.contains("WARNING["), css);
  }

  @Test
  public void testStrayClosingBraceRecovers() throws LessException {
    String css = compile("}\n.a { color: red; }\n", fixedSafe());
    assertTrue(css.contains(".a"), css);
    assertRecovered(css);
  }

  @Test
  public void testUnclosedBlockTruncates() throws LessException {
    String css = compile(".a { color: red;\n", fixedSafe());
    assertTrue(css.contains("color: red"), css);
    assertRecoveredWith(css, "truncated");
  }

  @Test
  public void testReleasedLevelSafeNoRecoveryWarnings() throws LessException {
    // At the released level the legacy behaviors are active. Safe mode has
    // nothing to recover and no warnings are emitted.
    String css = compile(MEDIA_BLOCKLESS, releasedSafe());
    assertTrue(css.contains("#content"), css);
    assertFalse(css.contains("WARNING["), css);
  }

  @Test
  public void testEscapedQuoteDoesNotDesyncRecovery() throws LessException {
    // A string containing an escaped quote must not corrupt the sync
    // scan. The following declaration on the same block survives.
    String css = compile(".a {\n  !!! \"a\\\"b\";\n  y: 1;\n}\n", fixedSafe());
    assertTrue(css.contains("y: 1"), css);
    assertTrue(!css.contains("truncated"), css);
    assertRecovered(css);

    // Top-level shape: the next statement survives.
    css = compile("!!! \"a\\\"b\";\ny: 1;\n", fixedSafe());
    assertTrue(css.contains("y: 1"), css);
    assertTrue(!css.contains("truncated"), css);
  }

  @Test
  public void testLineNumbersAfterRecovery() throws LessException {
    // After a recovery jump the incremental counters are fast-forwarded,
    // so nodes parsed after the jump carry correct positions (tracing
    // shows lineOffset + 1).
    LessOptions opts = fixedSafe();
    opts.tracing(true);
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(COMPILER);
    String css = COMPILER.compile("!!!\ny: 1;\n.z { color: red; }\n", ctx);
    assertTrue(css.contains("':3'"), css);
  }

  @Test
  public void testStrictStillFails() {
    // Strict mode at the fixed level must keep failing exactly as before.
    for (String raw : new String[] { MEDIA_BLOCKLESS, VAR_PAREN, INVALID_ADDITION }) {
      try {
        compile(raw, fixedStrict());
        fail("expected strict compile to fail: " + raw);
      } catch (LessException e) {
        // expected
      }
    }
  }
}
