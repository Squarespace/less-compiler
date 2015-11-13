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

import static com.squarespace.less.model.Operator.DIVIDE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.Units;
import com.squarespace.less.parse.Parselets;


public class ArgumentsTest extends LessTestBase {

  @Test
  public void testEquals() {
    Argument argXY = arg("x", anon("y"));
    Argument argXZ = arg("x", anon("z"));

    assertEquals(args(';'), args(';'));
    assertEquals(args(';', argXY), args(';', argXY));
    assertEquals(args(',', argXY, argXZ), args(',', argXY, argXZ));

    assertNotEquals(args(','), null);
    assertNotEquals(args(','), args(';'));
    assertNotEquals(args(','), args(';', argXZ));
    assertNotEquals(args(',', argXY), args(';', argXZ));
  }

  @Test
  public void testModelReprSafety() {
    arg(null, anon("z")).toString();
    arg("y", anon("z")).toString();
  }

  @Test
  public void testArguments() throws LessException {
    LessHarness h = new LessHarness(Parselets.MIXIN_CALL_ARGS);

    h.parseEquals("()",
        args(','));

    h.parseEquals("(@b)",
        args(',', arg(null, var("@b"))));

    h.parseEquals("(@b: 12px)",
        args(',', arg("@b", dim(12, Units.PX))));

    h.parseEquals("('@{x} y @{z}')",
        args(',', arg(null, quoted('\'', false, var("@x", true), anon(" y "), var("@z", true)))));

    h.parseEquals("(@a @b, @c)",
        args(',', arg(null, expn(var("@a"), var("@b"))), arg(null, var("@c"))));

    h.parseEquals("(@a: 1,2; @b: 2)",
        args(';', arg("@a", expnlist(dim(1), dim(2))), arg("@b", dim(2))));

    h.parseEquals("(1,2; @b)",
        args(';', arg(null, expnlist(dim(1), dim(2))), arg(null, var("@b"))));

    h.parseEquals("(1; 2/16;)",
        args(';', arg(null, dim(1)), arg(null, oper(DIVIDE, dim(2), dim(16)))));
  }

}
