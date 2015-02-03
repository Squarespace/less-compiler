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

import static com.squarespace.less.ExecuteErrorType.ARG_COUNT;
import static com.squarespace.less.ExecuteErrorType.INVALID_ARG;
import static com.squarespace.less.model.Unit.PX;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Parselets;


public class MathFunctionsTest extends LessTestBase {

  // TODO: testAbs

  // TODO: testASin

  // TODO: testACos

  // TODO: testATan

  @Test
  public void testCeil() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    // Ceiling / floor
    h.evalEquals("ceil(1.1)", dim(2));
    h.evalEquals("ceil(1.9)", dim(2));
    h.evalEquals("ceil(-1.1)", dim(-1));
    h.evalEquals("ceil(-1.9)", dim(-1));
  }

  // TODO: testCos

  @Test
  public void testFloor() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalEquals("floor(1.1)", dim(1));
    h.evalEquals("floor(1.9)", dim(1));
    h.evalEquals("floor(-1.1)", dim(-2));
    h.evalEquals("floor(-1.9)", dim(-2));
    h.evalFails("floor('dim')", INVALID_ARG);
  }

  @Test
  public void testMax() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    // Expects at least 1 arg
    h.evalFails("max()", ARG_COUNT);

    h.evalEquals("max(1)", dim(1));
    h.evalEquals("max(1px)", dim(1, PX));

    h.evalEquals("max(1, 2)", dim(2));
    h.evalEquals("max(2, 1)", dim(2));

    h.evalEquals("max(-100, -50, 50, -25)", dim(50));
    h.evalEquals("max(3px, 2px, 1px)", dim(3, PX));

    h.evalEquals("max(1px, 2turn)", anon("max(1px, 2turn)"));
    h.evalEquals("max(red, 1, blue)", anon("max(red, 1, blue)"));
  }

  @Test
  public void testMin() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("min()", ARG_COUNT);

    h.evalEquals("min(1)", dim(1));
    h.evalEquals("min(1px)", dim(1, PX));

    h.evalEquals("min(1, 2)", dim(1));
    h.evalEquals("min(2, 1)", dim(1));

    h.evalEquals("min(-100, -50, 50, -25)", dim(-100));
    h.evalEquals("min(3px, 2px, 1px)", dim(1, PX));

    h.evalEquals("min(1px, 2turn)", anon("min(1px, 2turn)"));
    h.evalEquals("min(red, 1, blue)", anon("min(red, 1, blue)"));
  }

  // TODO: testMod

  @Test
  public void testPercentage() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    // Decimal to percentage
    h.evalEquals("percentage(1)", dim(100, Unit.PERCENTAGE));
    h.evalEquals("percentage(.25)", dim(25, Unit.PERCENTAGE));
    h.evalEquals("percentage(0.0)", dim(0, Unit.PERCENTAGE));
    h.evalEquals("percentage(-2)", dim(-200, Unit.PERCENTAGE));
  }

  // TODO: testPi

  // TODO: testPow

  @Test
  public void testRound() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    // Rounding
    h.evalEquals("round(-12.2)", dim(-12));
    h.evalEquals("round(1.57, -5)", dim(2));
    h.evalEquals("round(2.1)", dim(2));
    h.evalEquals("round(3.4px)", dim(3, Unit.PX));
    h.evalEquals("round(3.5px)", dim(4, Unit.PX));
    h.evalEquals("round(3.5px, 1)", dim(3.5, Unit.PX));
    h.evalEquals("round(3.55px, 1)", dim(3.6, Unit.PX));
    h.evalEquals("round(-2.55, 2)", dim(-2.55));
    h.evalEquals("round(-2.57, 1)", dim(-2.6));
    h.evalEquals("round(-2.55, 1)", dim(-2.5));
    h.evalEquals("round(-2.52, 1)", dim(-2.5));
    h.evalEquals("round(12.123%, 1)", dim(12.1, Unit.PERCENTAGE));
  }

  // TODO: testSIn

  // TODO: testSqrt

  // TODO: testTan

}
