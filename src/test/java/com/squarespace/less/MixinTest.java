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

import static com.squarespace.less.core.Constants.TRUE;
import static com.squarespace.less.model.Operator.EQUAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;


public class MixinTest extends LessTestBase {

  @Test
  public void testEquals() {
    MixinParams params = params(param("@a"), param("@b", anon("c")));
    Guard guard = guard(cond(EQUAL, var("@a"), anon("b")));

    assertEquals(mixin(".foo"), mixin(".foo"));
    assertEquals(mixin(".foo", params, guard), mixin(".foo", params, guard));

    assertNotEquals(mixin(".foo"), null);
    assertNotEquals(mixin(".foo"), mixin(".bar"));
    assertNotEquals(mixin(".foo"), mixin(".foo", params, guard));
    assertNotEquals(mixin(".foo", params, guard), mixin(".foo"));
    assertNotEquals(mixin(".foo", params, null), mixin(".foo", params, guard));
  }

  @Test
  public void testModelReprSafety() {
    MixinParams params = params(param("@a"), param("@b", anon("c")));
    Guard guard = guard(cond(EQUAL, var("@a"), anon("b")));

    mixin(".foo").toString();
    mixin("#ns", params, guard).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness();

    Stylesheet exp = stylesheet();
    Node mixin = mixin(".x", params(param("@a")), guard(cond(EQUAL, var("@b"), TRUE, true)));
    exp.add(mixin);

    h.parseEquals(".x(@a) when not (@b) { }", exp);
  }

}
