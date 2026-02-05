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

import org.testng.annotations.Test;

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessErrorType;
import com.squarespace.less.LessException;
import com.squarespace.less.SyntaxErrorType;
import com.squarespace.less.LessOptions;


/**
 * The matrix contract: every patch row pins its (patch x level x mode)
 * behavior (status, strict error type, recovery-warning presence, and
 * whether the rendered body is unchanged from the released level).
 *
 * <p>The strict-mode cells were already pinned individually. This test
 * carries the whole table as a contract. Rows for the threshold-2
 * patches extend the array as the gate commits land, and the level
 * loop runs {@code 0..maxThreshold} so higher thresholds are exercised
 * automatically.
 */
public class RecoveryMatrixTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  /**
   * One patch row.
   *
   * @param errorAtFixed true for reject-fixes: strict mode errors at
   *     levels {@code >= threshold} and the released levels compile
   *     cleanly. False for accept-fixes: the legacy behavior errors below
   *     {@code threshold} and the fixed levels compile cleanly.
   * @param cssUnchangedAtFix true when, at a fixed level in safe mode,
   *     the rendered body equals the released (level 0) body. The
   *     recovery warning is then the only observable signal (BUG1).
   */
  static final class Row {

    final String name;
    final String source;
    final int threshold;
    final LessErrorType errorType;
    final boolean errorAtFixed;
    final boolean cssUnchangedAtFix;

    Row(String name, String source, int threshold, LessErrorType errorType,
        boolean errorAtFixed, boolean cssUnchangedAtFix) {
      this.name = name;
      this.source = source;
      this.threshold = threshold;
      this.errorType = errorType;
      this.errorAtFixed = errorAtFixed;
      this.cssUnchangedAtFix = cssUnchangedAtFix;
    }
  }

  private static final Row[] ROWS = {
      new Row("BUG1",
          ".a { + }\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, true, true),
      new Row("BUG2",
          "@media only screen and (max-width: 640px)\n#content {\n  padding-top: 50px;\n}\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, true, false),
      new Row("BUG3",
          "@dk-gray: #333;\n.dark-bg {\n  background-color: @dk-gray();\n}\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, true, false),
      new Row("BUG4",
          "@foo: 10px;\n.a {\n  width: @foo + px;\n}\n",
          1, SyntaxErrorType.INCOMPLETE_PARSE, true, false),
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

  private static Outcome run(String source, int level, boolean safe) {
    LessOptions opts = new LessOptions();
    opts.compatLevel(level);
    opts.safeMode(safe);
    try {
      return new Outcome(true, COMPILER.compile(source, new LessContext(opts)), null);
    } catch (LessException e) {
      return new Outcome(false, null, e.primaryError().type());
    }
  }

  private static int warningCount(String css) {
    int count = 0;
    int idx = 0;
    while ((idx = css.indexOf("WARNING[", idx)) >= 0) {
      count++;
      idx += 8;
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
      Outcome released = run(row.source, 0, true);
      if (!released.ok) {
        report.append(row.name).append(": released (level 0) compile failed\n");
        failures++;
        continue;
      }
      for (int level = 0; level <= maxLevel; level++) {
        boolean fixed = level >= row.threshold;

        // Strict mode.
        Outcome strict = run(row.source, level, false);
        if (row.errorAtFixed ? fixed : !fixed) {
          // The error cell for this row.
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
          // The clean cell for this row.
          report.append(row.name).append(" L").append(level).append(" strict: expected ok, got ")
              .append(strict.errorType).append('\n');
          failures++;
        } else if (warningCount(strict.css) != 0) {
          report.append(row.name).append(" L").append(level)
              .append(" strict: unexpected recovery warnings\n");
          failures++;
        }

        // Safe mode: always compiles. Warnings appear exactly on the
        // error cells, and on reject-fix fixed levels the body either
        // stays or changes per the row contract.
        Outcome safe = run(row.source, level, true);
        if (!safe.ok) {
          report.append(row.name).append(" L").append(level).append(" safe: expected ok, got ")
              .append(safe.errorType).append('\n');
          failures++;
          continue;
        }
        int warns = warningCount(safe.css);
        if (row.errorAtFixed ? fixed : !fixed) {
          if (warns == 0) {
            report.append(row.name).append(" L").append(level)
                .append(" safe: expected a recovery warning\n");
            failures++;
          }
          if (row.errorAtFixed && fixed) {
            boolean same = body(safe.css).equals(body(released.css));
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
        }
      }
    }
    if (failures > 0) {
      fail(failures + " matrix contract violations:\n" + report);
    }
  }
}
