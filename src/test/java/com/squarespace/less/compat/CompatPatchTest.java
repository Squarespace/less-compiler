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
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.less.ExecuteErrorType;
import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;


/**
 * Both-behavior checks for the generation-2 patches: the default level
 * keeps the released behavior, level 0 applies the fix.
 */
public class CompatPatchTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  private String compile(String raw, LessOptions opts) throws LessException {
    return COMPILER.compile(raw, new LessContext(opts));
  }

  private static LessOptions level(int level) {
    LessOptions opts = new LessOptions();
    opts.compatLevel(level);
    return opts;
  }

  @Test
  public void testSelectorComplexityOverflow() throws LessException {
    // 65 comma siblings at 64 nesting levels: the cartesian product of
    // combined selectors exceeds the complexity threshold.
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

    // Legacy default: the overflow is swallowed, compile succeeds.
    compile(less.toString(), new LessOptions());

    // Level 0: the compile fails with SELECTOR_TOO_COMPLEX.
    try {
      compile(less.toString(), level(0));
      fail("expected SELECTOR_TOO_COMPLEX at level 0");
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), ExecuteErrorType.SELECTOR_TOO_COMPLEX);
    }
  }
}
