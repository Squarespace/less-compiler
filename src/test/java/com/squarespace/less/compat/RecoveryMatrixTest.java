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
import com.squarespace.less.LessErrorType;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.SyntaxErrorType;


/**
 * The matrix contract: every patch row pins its (patch x level x mode)
 * behavior (status, strict error type, recovery-warning presence, and
 * whether the rendered body is unchanged from the released level).
 *
 * <p>Three polarities cover the fix families: {@code REJECT_FIX} (the
 * fix rejects legacy junk: strict errors at the fixed level, safe
 * recovers), {@code ACCEPT_FIX} (the fix accepts input legacy rejected:
 * strict errors below the threshold, safe recovers there), and
 * {@code OUTPUT_FIX} (the fix only changes rendered output: css* signal,
 * never an error or recovery warning).
 *
 * <p>REPLACE_REGEX_GROUPS has no row: its function is not registered in
 * the default function table, so it errors at every level through the
 * default compiler surface.
 */
public class RecoveryMatrixTest {

  private enum Polarity {
    REJECT_FIX,
    ACCEPT_FIX,
    OUTPUT_FIX
  }

  private static final LessCompiler COMPILER = new LessCompiler();

  /**
   * One patch row.
   *
   * @param cssUnchangedAtFix for REJECT_FIX rows: true when, at a fixed
   *     level in safe mode, the rendered body equals the released (level
   *     0) body. The recovery warning is then the only observable
   *     signal (BUG1).
   * @param files optional import-fixture files (absolute-keyed), rows
   *     with files compile with a root path so imports resolve.
   */
  static final class Row {

    final String name;
    final String source;
    final int threshold;
    final LessErrorType errorType;
    final Polarity polarity;
    final boolean cssUnchangedAtFix;
    final Map<Path, String> files;

    Row(String name, String source, int threshold, LessErrorType errorType, Polarity polarity,
        boolean cssUnchangedAtFix, Map<Path, String> files) {
      this.name = name;
      this.source = source;
      this.threshold = threshold;
      this.errorType = errorType;
      this.polarity = polarity;
      this.cssUnchangedAtFix = cssUnchangedAtFix;
      this.files = files;
    }
  }

  private static Map<Path, String> filesOf(String name, String content) {
    Map<Path, String> map = new HashMap<>();
    map.put(Paths.get(".").resolve(name).toAbsolutePath().normalize(), content);
    return map;
  }

  /** 
   * 65 comma-siblings nested 63 levels deep: exceeds the 4096 element limit. 
   */
  private static String complexitySource() {
    StringBuilder less = new StringBuilder();
    for (int i = 0; i < 65; i++) {
      if (i > 0) {
        less.append(", ");
      }
      less.append(".a").append(i);
    }
    less.append(" {\n");
    for (int d = 0; d < 63; d++) {
      less.append("  ").append(".b").append(d).append(" {\n");
    }
    less.append("    color: red;\n");
    for (int d = 0; d < 63; d++) {
      less.append("  }");
    }
    less.append("\n}\n");
    return less.toString();
  }

  private static final Row[] ROWS = {
      // Threshold 1: the former safe-mode tolerances
      new Row("BUG1",
          ".a { + }\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, Polarity.REJECT_FIX, true, null),
      new Row("BUG2",
          "@media only screen and (max-width: 640px)\n#content {\n  padding-top: 50px;\n}\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, Polarity.REJECT_FIX, true, null),
      new Row("BUG3",
          "@dk-gray: #333;\n.dark-bg {\n  background-color: @dk-gray();\n}\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, Polarity.REJECT_FIX, false, null),
      new Row("BUG4",
          "@foo: 10px;\n.a {\n  width: @foo + px;\n}\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, Polarity.REJECT_FIX, false, null),

      // Threshold 2: the generation-2 patches
      new Row("ATTR_SELECTOR_UNTERMINATED",
          "a[href {\n  color: red;\n}\n",
          2, SyntaxErrorType.INCOMPLETE_PARSE, Polarity.REJECT_FIX, false, null),
      new Row("SELECTOR_COMPLEXITY_OVERFLOW",
          complexitySource(),
          2, ExecuteErrorType.SELECTOR_TOO_COMPLEX, Polarity.REJECT_FIX, false, null),
      new Row("IMPORT_URL_INLINE",
          "@import url(\"a.less\");\n",
          2, null, Polarity.OUTPUT_FIX, false, filesOf("a.less", ".a { color: red; }\n")),
      new Row("NONFINITE_AS_ZERO",
          ".nf { w: sqrt(-1); }\n",
          2, null, Polarity.OUTPUT_FIX, false, null),
          
      // With the default strict division contract the fixed behavior
      // errors (DIVIDE_BY_ZERO). The lenient contract only applies with
      // opts.strict(false).
      new Row("MOD_ZERO_STRICT",
          ".mz { w: mod(10, 0); }\n",
          2, ExecuteErrorType.DIVIDE_BY_ZERO, Polarity.REJECT_FIX, false, null),
      new Row("CONVERT_INCOMPATIBLE_UNITS",
          ".cv { w: convert(16px, em); }\n",
          2, ExecuteErrorType.INCOMPATIBLE_UNITS, Polarity.REJECT_FIX, false, null),
      new Row("VARIADIC_NAMED_ARG",
          ".m(@b...) { p: @b; }\n.x { .m(@b: 1); }\n",
          2, ExecuteErrorType.ARG_NAMED_NOTFOUND, Polarity.ACCEPT_FIX, false, null),
      new Row("ARGUMENTS_ORDER",
          ".m(@a, @b) { p: @arguments; }\n.x { .m(@b: 2, @a: 1); }\n",
          2, null, Polarity.OUTPUT_FIX, false, null),
      new Row("GUARD_COMPARE_UNCOMPARABLE",
          ".m(@a) when (@a != 10px) { p: 1; }\n.x { .m(red); }\n",
          2, null, Polarity.OUTPUT_FIX, false, null),
      new Row("IMPORT_EXT_CASE",
          "@import \"A.LESS\";\n",
          2, SyntaxErrorType.IMPORT_ERROR, Polarity.ACCEPT_FIX, false, filesOf("A.LESS", ".a { color: red; }\n")),
      new Row("IMPORT_ONCE_SUPPRESS",
          "@import 'f.less';\n@import-once 'f.less';\n",
          2, null, Polarity.OUTPUT_FIX, false, filesOf("f.less", ".f { color: red; }\n")),
      new Row("COLOR_BLEND_ALPHA",
          ".blend { color: multiply(rgba(255, 0, 0, 0.5), rgba(0, 0, 255, 0.25)); }\n",
          2, null, Polarity.OUTPUT_FIX, false, null),
      new Row("COLOR_CHANNEL_PRECISION",
          ".cp { c: #fff * 0.5; }\n",
          2, null, Polarity.OUTPUT_FIX, false, null),
  };

  private static final class Outcome {

    final boolean ok;
    final String css;
    final LessErrorType errorType;

    Outcome(boolean ok, String css, LessErrorType errorType) {
      this.ok = ok;
      this.css = css;
      this.errorType = errorType;
    }
  }

  private static Outcome run(String source, int level, boolean safe, Map<Path, String> files) {
    LessOptions opts = new LessOptions();
    opts.compatLevel(level);
    opts.safeMode(safe);
    try {
      String css;
      if (files == null) {
        LessContext ctx = new LessContext(opts);
        ctx.setCompiler(COMPILER);
        css = COMPILER.compile(source, ctx);
      } else {
        LessContext ctx = new LessContext(opts, new HashMapLessLoader(files));
        ctx.setCompiler(COMPILER);
        // Root path lets the importer resolve the fixture files.
        css = COMPILER.compile(source, ctx, Paths.get("."), Paths.get("t.less"));
      }
      return new Outcome(true, css, null);
    } catch (LessException e) {
      return new Outcome(false, null, e.primaryError().type());
    }
  }

  /** Recovery warnings use the "raised during recovery" phrase. */
  private static int recoveryWarningCount(String css) {
    int count = 0;
    int idx = 0;
    while ((idx = css.indexOf("raised during recovery", idx)) >= 0) {
      count++;
      idx += 22;
    }
    return count;
  }

  private static String body(String css) {
    return css.replaceAll("/\\* WARNING\\[[0-9]+\\][^\\n]*\\*/\\n?", "").trim();
  }

  @Test
  public void testMatrixContract() {
    StringBuilder report = new StringBuilder();
    int failures = 0;
    int maxLevel = Patch.maxThreshold();
    for (Row row : ROWS) {
      // Released-level reference: safe mode always compiles, giving the
      // body that level-0 behavior would render.
      Outcome released = run(row.source, 0, true, row.files);
      String releasedBody = released.ok ? body(released.css) : "";
      for (int level = 0; level <= maxLevel; level++) {
        boolean fixed = level >= row.threshold;

        // Strict mode.
        Outcome strict = run(row.source, level, false, row.files);
        boolean expectError;
        switch (row.polarity) {
          case REJECT_FIX:
            expectError = fixed;
            break;
          case ACCEPT_FIX:
            expectError = !fixed;
            break;
          default:
            expectError = false;
            break;
        }
        if (expectError) {
          if (strict.ok) {
            report.append(row.name).append(" L").append(level).append(" strict: expected ")
                .append(row.errorType).append(", compiled\n");
            failures++;
          } else if (strict.errorType != row.errorType) {
            report.append(row.name).append(" L").append(level).append(" strict: expected ")
                .append(row.errorType).append(", got ").append(strict.errorType).append('\n');
            failures++;
          }
        } else if (!strict.ok) {
          report.append(row.name).append(" L").append(level).append(" strict: expected ok, got ")
              .append(strict.errorType).append('\n');
          failures++;
        } else if (recoveryWarningCount(strict.css) != 0) {
          report.append(row.name).append(" L").append(level)
              .append(" strict: unexpected recovery warnings\n");
          failures++;
        }

        // Safe mode: always compiles.
        Outcome safe = run(row.source, level, true, row.files);
        if (!safe.ok) {
          report.append(row.name).append(" L").append(level).append(" safe: expected ok, got ")
              .append(safe.errorType).append('\n');
          failures++;
          continue;
        }
        int warns = recoveryWarningCount(safe.css);
        boolean recoveryExpected;
        switch (row.polarity) {
          case REJECT_FIX:
            recoveryExpected = fixed;
            break;
          case ACCEPT_FIX:
            recoveryExpected = !fixed;
            break;
          default:
            recoveryExpected = false;
            break;
        }
        if (recoveryExpected) {
          if (warns == 0) {
            report.append(row.name).append(" L").append(level)
                .append(" safe: expected a recovery warning\n");
            failures++;
          }
          if (row.polarity == Polarity.REJECT_FIX && fixed) {
            boolean same = body(safe.css).equals(releasedBody);
            if (same != row.cssUnchangedAtFix) {
              report.append(row.name).append(" L").append(level).append(" safe: body ")
                  .append(same ? "unchanged but expected change" : "changed but expected unchanged")
                  .append('\n');
              failures++;
            }
          }
        } else if (warns != 0) {
          report.append(row.name).append(" L").append(level)
              .append(" safe: unexpected recovery warnings\n");
          failures++;
        } else {
          // OUTPUT_FIX rows: the css* signal at the fixed level.
          boolean same = body(safe.css).equals(releasedBody);
          if (fixed && same) {
            report.append(row.name).append(" L").append(level)
                .append(" safe: expected css change at the fixed level, body unchanged\n");
            failures++;
          }
        }
      }
    }
    if (failures > 0) {
      fail(failures + " matrix contract violations:\n" + report);
    }
  }
}
