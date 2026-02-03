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

import org.testng.annotations.Test;

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;


/**
 * End-to-end: the compat level drives parser legacy behaviors through the
 * public API. Default compiles legacy input, level 0 rejects it, overrides
 * restore individual behaviors.
 */
public class CompatCompileTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  private static final String MEDIA_BLOCKLESS = "" +
      "@media only screen and (max-width: 640px)\n" +
      "#content {\n" +
      "  padding-top: 50px;\n" +
      "}\n";

  private static final String VAR_PAREN = "" +
      "@dk-gray: #333;\n" +
      ".dark-bg {\n" +
      "  background-color: @dk-gray();\n" +
      "}\n";

  private static final String INVALID_ADDITION = "" +
      "@foo: 10px;\n" +
      ".a {\n" +
      "  width: @foo + px;\n" +
      "}\n";

  private String compile(String raw, LessOptions opts) throws LessException {
    LessContext ctx = new LessContext(opts);
    return COMPILER.compile(raw, ctx);
  }

  private void assertFails(String raw, LessOptions opts) {
    try {
      compile(raw, opts);
      assertFalse(true, "expected compile to fail: " + raw);
    } catch (LessException e) {
      // expected
    }
  }

  @Test
  public void testDefaultLevelCompilesLegacyInput() throws LessException {
    // Default level preserves the released safe-mode surface.
    String css = compile(MEDIA_BLOCKLESS, new LessOptions());
    assertTrue(css.contains("#content"), css);
    assertTrue(css.contains("padding-top: 50px"), css);

    css = compile(VAR_PAREN, new LessOptions());
    assertTrue(css.contains("#333"), css);

    compile(INVALID_ADDITION, new LessOptions());
  }

  @Test
  public void testFixedLevelRejectsLegacyInput() {
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    assertFails(MEDIA_BLOCKLESS, opts);
    assertFails(VAR_PAREN, opts);
    assertFails(INVALID_ADDITION, opts);
  }

  @Test
  public void testSafeModeFalseMatchesFixedLevel() {
    // safeMode(false) and the fully-fixed level must be equivalent.
    for (String raw : new String[] { MEDIA_BLOCKLESS, VAR_PAREN, INVALID_ADDITION }) {
      try {
        LessContext ctx = new LessContext(new LessOptions());
        COMPILER.compile(raw, ctx, null, null, false);
        assertFalse(true, "expected safeMode(false) compile to fail: " + raw);
      } catch (LessException e) {
        // expected
      }
      assertFails(raw, fixedOptions());
    }
  }

  @Test
  public void testOverridesRestoreIndividualBehaviors() throws LessException {
    // A per-site override forces one legacy behavior on at the fully
    // fixed level.
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());

    assertFails(MEDIA_BLOCKLESS, opts);
    opts.compatPatch(Patch.BUG2);
    String css = compile(MEDIA_BLOCKLESS, opts);
    assertTrue(css.contains("#content"), css);

    // Overrides are per-behavior: BUG2 on does not enable BUG3.
    assertFails(VAR_PAREN, opts);

    LessOptions fixed = new LessOptions();
    fixed.compatLevel(Patch.maxThreshold());
    fixed.compatPatch(Patch.BUG3);
    css = compile(VAR_PAREN, fixed);
    assertTrue(css.contains("#333"), css);
    assertFails(INVALID_ADDITION, fixed);
  }

  private LessOptions fixedOptions() {
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    return opts;
  }
}
