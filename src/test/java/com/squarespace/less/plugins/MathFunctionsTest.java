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
import static com.squarespace.less.model.Unit.RAD;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Parselets;


public class MathFunctionsTest extends LessTestBase {

  @Test
  public void testAbs() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("abs()", ARG_COUNT);
    h.evalFails("abs(1, 1)", ARG_COUNT);

    h.evalEquals("abs(-1)", dim(1));
    h.evalEquals("abs(-1px)", dim(1, PX));
    h.evalEquals("abs(1rad)", dim(1, RAD));
  }

  @Test
  public void testArcSin() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("asin()", ARG_COUNT);
    h.evalFails("asin(1, 1)", ARG_COUNT);

    h.evalEquals("asin(-0.8414709848078965)", dim(-1, RAD));
    h.evalEquals("asin(0)", dim(0, RAD));
    h.evalEquals("asin(2)", dim(Double.NaN, RAD));
  }

  @Test
  public void testArcCos() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("acos()", ARG_COUNT);
    h.evalFails("acos(1, 1)", ARG_COUNT);

    h.evalEquals("acos(0.5403023058681398)", dim(1, RAD));
    h.evalEquals("acos(1)", dim(0, RAD));
    h.evalEquals("acos(2)", dim(Double.NaN, RAD));
  }

  @Test
  public void testArcTan() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("atan()", ARG_COUNT);
    h.evalFails("atan(1, 1)", ARG_COUNT);

    h.evalEquals("atan(-1.5574077246549023)", dim(-1, RAD));
    h.evalEquals("atan(0)", dim(0, RAD));
    h.evalEquals("round(atan(22), 6)", dim(1.525373, RAD));
  }

  @Test
  public void testCeil() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("ceil()", ARG_COUNT);
    h.evalFails("ceil(1, 1)", ARG_COUNT);

    h.evalEquals("ceil(1.1)", dim(2));
    h.evalEquals("ceil(1.9)", dim(2));
    h.evalEquals("ceil(-1.1)", dim(-1));
    h.evalEquals("ceil(-1.9)", dim(-1));
  }

  @Test
  public void testCos() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("cos()", ARG_COUNT);
    h.evalFails("cos(1, 1)", ARG_COUNT);

    h.evalEquals("cos(1)", dim(0.5403023058681398));
    h.evalEquals("cos(1deg)", dim(0.9998476951563913));
    h.evalEquals("cos(1grad)", dim(0.9998766324816606));
  }

  @Test
  public void testFloor() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("floor()", ARG_COUNT);
    h.evalFails("floor(1, 1)", ARG_COUNT);


    h.evalEquals("floor(1.1)", dim(1));
    h.evalEquals("floor(1.9)", dim(1));
    h.evalEquals("floor(-1.1)", dim(-2));
    h.evalEquals("floor(-1.9)", dim(-2));
    h.evalFails("floor('dim')", INVALID_ARG);
  }

  @Test
  public void testMax() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

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

  @Test
  public void testMod() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("mod()", ARG_COUNT);

    h.evalEquals("mod(0cm, 0px)", dim(Double.NaN, Unit.CM));
    h.evalEquals("mod(11cm, 6px)", dim(5, Unit.CM));
    h.evalEquals("mod(-26%, -5)", dim(-1, Unit.PERCENTAGE));
  }

  @Test
  public void testPercentage() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("percentage()", ARG_COUNT);
    h.evalFails("percentage(1, 1)", ARG_COUNT);

    h.evalEquals("percentage(1)", dim(100, Unit.PERCENTAGE));
    h.evalEquals("percentage(.25)", dim(25, Unit.PERCENTAGE));
    h.evalEquals("percentage(0.0)", dim(0, Unit.PERCENTAGE));
    h.evalEquals("percentage(-2)", dim(-200, Unit.PERCENTAGE));
  }

  @Test
  public void testPI() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("pi(0)", ARG_COUNT);

    h.evalEquals("pi()", dim(Math.PI));
  }

  @Test
  public void testPow() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("pow()", ARG_COUNT);
    h.evalFails("pow(1)", ARG_COUNT);
    h.evalFails("pow(1, 2, 3)", ARG_COUNT);

    h.evalEquals("pow(0cm, 0px)", dim(1, Unit.CM));
    h.evalEquals("pow(25, -2)", dim(0.0016));
    h.evalEquals("pow(25, 0.5)", dim(5));
    h.evalEquals("pow(-25, 0.5)", dim(Double.NaN));
    h.evalEquals("pow(-25%, -0.5)", dim(Double.NaN, Unit.PERCENTAGE));
  }

  @Test
  public void testRound() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("round()", ARG_COUNT);

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

  @Test
  public void testSin() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("sin()", ARG_COUNT);
    h.evalFails("sin(1, 1)", ARG_COUNT);

    h.evalEquals("sin(1)", dim(0.8414709848078965));
    h.evalEquals("sin(1deg)", dim(0.01745240643728351));
    h.evalEquals("sin(1grad)", dim(0.015707317311820675));
  }

  @Test
  public void testSqrt() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalFails("sqrt()", ARG_COUNT);
    h.evalFails("sqrt(1, 1)", ARG_COUNT);

    h.evalEquals("sqrt(-3)", dim(Double.NaN));
    h.evalEquals("sqrt(25cm)", dim(5, Unit.CM));
    h.evalEquals("sqrt(18.6%)", dim(4.312771730569565, Unit.PERCENTAGE));
  }

  @Test
  public void testTan() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.evalEquals("tan(1)", dim(1.5574077246549023));
    h.evalEquals("tan(1deg)", dim(0.017455064928217585));
    h.evalEquals("tan(1grad)", dim(0.015709255323664916));
  }

}
