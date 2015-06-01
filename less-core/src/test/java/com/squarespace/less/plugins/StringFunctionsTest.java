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
import com.squarespace.less.parse.Parselets;


public class StringFunctionsTest extends LessTestBase {

  @Test
  public void testE() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalEquals("e('foo')", anon("foo"));
    h.evalEquals("e('-moz-foo-bar')", anon("-moz-foo-bar"));
  }

  @Test
  public void testEscape() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalEquals("escape(' (hi) ')", anon("%20%28hi%29%20"));
  }

  @Test
  public void testFormat() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    // Format
    h.evalEquals("%('%s, %s', 12px, 'foo')", quoted('\'', false, anon("12px, foo")));
    h.evalEquals("%('%s %A', '()', #ff1133)", quoted('\'', false, anon("() %23f13")));
    h.evalEquals("%('%s %A', '()', #f00)", quoted('\'', false, anon("() %23f00")));
  }

  // TODO: testReplace

}
