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
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class CommentTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(comment("x", true), comment("x", true));
    assertEquals(comment("y", false), comment("y", false));
    assertNotEquals(comment("x", false), comment("y", false));
    assertNotEquals(comment("x", false), comment("x", true));
  }

  @Test
  public void testModelReprSafety() {
    comment("x", false).toString();
  }

  @Test
  public void testComment() throws LessException {
    LessHarness h = new LessHarness(Parselets.COMMENT);
    h.parseEquals("/*** a*b/c ***/", comment("** a*b/c **", true));
    h.parseEquals("/* foo */", comment(" foo ", true));
    h.parseEquals("// foo", comment(" foo", false));
    h.parseEquals("// a //", comment(" a //", false));
    h.parseFails("/x", SyntaxErrorType.INCOMPLETE_PARSE);
  }

  @Test
  public void testBangComment() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    LessOptions opts = new LessOptions(true);

    String actual;
    String source = "/* foo *//*! bar *//* foo */";

    actual = h.execute(source, opts);
    assertEquals(actual, "/*! bar */\n");

    // When comments are being ignored, ensure hang comments are still captured
    opts.ignoreComments(true);
    actual = h.execute(source, opts);
    assertEquals(actual, "/*! bar */\n");
  }
}
