/**
 * Copyright, 2015, Squarespace, Inc.
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

import static com.squarespace.less.SyntaxErrorType.JAVASCRIPT_DISABLED;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class JavascriptTest extends LessTestBase {

  @Test
  public void testJavaScriptError() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);
    h.parseFails("color: `'red' + 'dish-' + 'blue'`;", JAVASCRIPT_DISABLED);
  }

  /* TODO: restore this test once parser warnings are supported
  @Test
  public void testJavaScriptWarning() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);
    LessOptions opts = new LessOptions();
    opts.strict(false);
    String result = h.execute("color: `'0x' + 'f00'`;", opts);
  }
  */

}
