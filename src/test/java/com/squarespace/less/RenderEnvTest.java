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
import static org.testng.Assert.assertSame;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.less.compat.Patch;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.RenderEnv;
import com.squarespace.less.exec.RenderFrame;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;


public class RenderEnvTest extends LessTestBase {

  @Test
  public void testPushLeavesStateUnchangedOnOverflow() throws LessException {
    // At the fixed compat level and strict mode, a selector complexity
    // overflow is a hard error: no legacy fallback, no safe-mode
    // truncation. push() must not commit its new frame/depth until the
    // merge that can throw has succeeded. Otherwise a caller further up
    // the stack that recovers from the exception (safe mode) would
    // inherit a half-pushed env: later siblings would attach under the
    // wrong frame, and the model could fail its end-of-render depth
    // check.
    LessOptions opts = new LessOptions();
    opts.compatLevel(Patch.maxThreshold());
    LessContext ctx = new LessContext(opts);

    RenderEnv env = new RenderEnv(ctx);
    env.push(manySelectors(100, "a"));
    RenderFrame before = env.frame();

    try {
      env.push(manySelectors(100, "b"));
      fail("Expected a selector complexity overflow");
    } catch (LessException expected) {
      // expected: 100 ancestor selectors x 100 current selectors exceeds
      // the complexity threshold
    }

    assertSame(env.frame(), before, "a failed push must not change the current frame");
    assertEquals(env.frame().depth(), 1, "a failed push must not advance the stack depth");
  }

  private Ruleset manySelectors(int count, String prefix) {
    Selector[] sels = new Selector[count];
    for (int i = 0; i < count; i++) {
      sels[i] = selector(element("." + prefix + i));
    }
    return new Ruleset(selectors(sels));
  }

}
