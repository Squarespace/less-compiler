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
 * outcomes are pinned by the matrix contract test.
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
    // BUG2 at the fixed level: the directive is dropped and the file
    // compiles with a warning. Recovery is at statement granularity, so
    // the block that follows the failed directive is part of the dropped
    // region (unlike the legacy DUMMY_MEDIA path, which keeps it).
    String css = compile(MEDIA_BLOCKLESS, fixedSafe());
    assertFalse(css.contains("#content"), css);
    assertRecoveredWith(css, "skipped invalid statement");
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
  public void testGarbageInputWarnsNoOutput() throws LessException {
    String css = compile("!!!\ngarbage\n###", fixedSafe());
    assertRecoveredWith(css, "produced no output");
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
