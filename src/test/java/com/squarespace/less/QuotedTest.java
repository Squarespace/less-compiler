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
import com.squarespace.less.parse.LessSyntax;


public class QuotedTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(quoted('"', false, "foo", "bar"), quoted('"', false, "foo", "bar"));
    assertEquals(quoted('"', false, var("@foo"), "bar"), quoted('"', false, var("@foo"), "bar"));

    assertNotEquals(quoted('"', false, "foo", "bar"), null);
    assertNotEquals(quoted('"', false, "foo", "bar"), anon("foobar"));
    assertNotEquals(quoted('"', false, "foo", "bar"), quoted('\'', false, "foo", "bar"));
    assertNotEquals(quoted('"', false, "foo", "bar"), quoted('"', false, "bar"));
  }

  @Test
  public void testModelReprSafety() {
    quoted('"', false, "foo").toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.QUOTED);

    h.parseEquals("'single'", quoted('\'', false, "single"));
    h.parseEquals("\"double\"", quoted('"', false, "double"));
    h.parseEquals("\"'foo'\"", quoted('"', false, "'foo'"));
    h.parseEquals("'\"foo\"'", quoted('\'', false, "\"foo\""));
    h.parseEquals("'\\\\'", quoted('\'', false, "\\\\"));

    h.parseEquals("'@{a} . @{b}'", quoted('\'', false, var("@a", true), " . ", var("@b", true)));
    h.parseEquals("'@ {a}'", quoted('\'', false, "@ {a}"));
    h.parseEquals("\"@{ x }\"", quoted('"', false, "@{ x }"));
    h.parseEquals("\" @{\"", quoted('"', false, " @{"));

    h.parseEquals("~'foo'", quoted('\'', true, anon("foo")));
    h.parseEquals("~\"@@{var}\"", quoted('"', true, var("@@var", true)));

    h.parseEquals("'foo", quoted('\'', false, "foo"));
    h.parseEquals("'foo", quoted('\'', false, "foo"));
    h.parseEquals("'foo\\", quoted('\'', false, "foo\\"));

    h.parseFails("'foo\n", SyntaxErrorType.QUOTED_BARE_LF);
    h.parseFails("~", SyntaxErrorType.INCOMPLETE_PARSE);
  }

}
