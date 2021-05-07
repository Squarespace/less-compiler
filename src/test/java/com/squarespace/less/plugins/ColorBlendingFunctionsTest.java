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


public class ColorBlendingFunctionsTest extends LessTestBase {

  @Test
  public void testAverage() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.FUNCTION_CALL);

    h.evalEquals("average(#888, #444)", color("#666"));
    h.evalEquals("difference(#888, #444)", color("#444"));
  }

  // TODO: testDifference

  // TODO: testExclusion

  // TODO: testHardlight

  // TODO: testMultiply

  // TODO: testNegation

  // TODO: testOverlay

  // TODO: testScreen

  // TODO: testSoftlight

}
