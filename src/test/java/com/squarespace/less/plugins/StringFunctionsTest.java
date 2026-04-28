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

package com.squarespace.less.plugins;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.LessSyntax;


public class StringFunctionsTest extends LessTestBase {

  @Test
  public void testE() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.FUNCTION_CALL);

    h.evalEquals("e('foo')", anon("foo"));
    h.evalEquals("e('-moz-foo-bar')", anon("-moz-foo-bar"));
  }

  @Test
  public void testEscape() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.FUNCTION_CALL);

    h.evalEquals("escape(' (hi) ')", anon("%20%28hi%29%20"));
  }

  @Test
  public void testFormat() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.FUNCTION_CALL);

    // Format
    h.evalEquals("%('%s, %s', 12px, 'foo')", quoted('\'', false, anon("12px, foo")));
    h.evalEquals("%('%s %A', '()', #ff1133)", quoted('\'', false, anon("() %23f13")));
    h.evalEquals("%('%s %A', '()', #f00)", quoted('\'', false, anon("() %23f00")));

    // Only s/S/d/D/a/A consume args; other %X passes through literally
    // and consumes no argument; %% is a literal percent.
    h.evalEquals("%('100% off', 5)", quoted('\'', false, anon("100% off")));
    h.evalEquals("%('%x', 1)", quoted('\'', false, anon("%x")));
    h.evalEquals("%('%s %% %d', 'a', 2)", quoted('\'', false, anon("a % 2")));
    h.evalEquals("%('%s %x %d', 1, 2)", quoted('\'', false, anon("1 %x 2")));

    // Uppercase non-specifier also passes through untouched.
    h.evalEquals("%('%Z', 1)", quoted('\'', false, anon("%Z")));
  }

  // TODO: testReplace

}
