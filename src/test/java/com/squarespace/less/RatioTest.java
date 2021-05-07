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

import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.LessSyntax;


public class RatioTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(ratio("3/4"), ratio("3/4"));

    assertNotEquals(ratio("3/4"), null);
    assertNotEquals(ratio("3/4"), anon("foo"));
    assertNotEquals(ratio("3/4"), ratio("2/3"));
  }

  @Test
  public void testModelReprSafety() {
    ratio("3/4").toString();
  }

  @Test
  public void testRatio() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.RATIO);

    h.parseEquals("15/30", ratio("15/30"));
    h.parseFails("foo/bar", INCOMPLETE_PARSE);
  }

}
