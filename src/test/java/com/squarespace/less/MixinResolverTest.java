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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.MixinMatcher;
import com.squarespace.less.exec.MixinResolver;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Stylesheet;


public class MixinResolverTest extends LessTestBase {

  @Test
  public void testResolver() throws LessException {
    Context ctx = new Context();
    ExecEnv env = ctx.newEnv();
    MixinCall call = mixincall(selector(element("#ns"), element(".m1")));
    MixinMatcher matcher = new MixinMatcher(env, call);

    Stylesheet sheet = stylesheet();
    Ruleset r1 = ruleset(selector(element("#ns")));
    Ruleset r2 = ruleset(selector(element(".m1")));
    r2.add(rule(prop("color"), color("white")));
    r1.add(r2);
    sheet.add(r1);

    MixinResolver resolver = new MixinResolver();
    resolver.reset(matcher);
    assertTrue(resolver.match(sheet.block()));
  }

//  @Test  // enable for performance testing and profiling
  public void testBindSpeed() throws Exception {
    int iters = 1000;
    Context ctx = new Context();
    ExecEnv env = ctx.newEnv();
    MixinCallArgs args = args(',', arg("a", dim(1)), arg("b", dim(2)), arg("c", dim(3)), arg(dim(17)));
    MixinCall call = mixincall(selector(element(".mixin")), args);

    MixinParams params = params(param("a"), param("b"), param("c"), param("rest", true));
    MixinMatcher matcher = new MixinMatcher(env, call);

    for (int i = 0; i < iters; i++) {
      matcher.bind(params);
    }
  }

}
