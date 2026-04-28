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

import static com.squarespace.less.model.UnitConversions.factor;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.model.Unit;


public class UnitConversionsTest {

  @Test
  public void testBasic() {
    // A little sparse, but this exists to simply confirm that the order of the
    // arguments are correct: factor(from, to).
    assertEquals(factor(Unit.IN, Unit.PX), 96.0);
    assertEquals(factor(Unit.PX, Unit.IN), 1.0 / 96.0);
    assertEquals(factor(Unit.KHZ, Unit.HZ), 1000.0);
    assertEquals(factor(Unit.HZ, Unit.KHZ), 1 / 1000.0);
  }

  /**
   * Full length-unit matrix: 1in = 2.54cm = 25.4mm = 96px = 72pt = 6pc.
   * Every ordered pair must satisfy factor(a, b) = unit-per-inch(b) /
   * unit-per-inch(a), and the inverse must hold in the reverse direction.
   */
  @Test
  public void testLengthMatrix() {
    Unit[] units = { Unit.IN, Unit.CM, Unit.MM, Unit.PX, Unit.PT, Unit.PC };
    double[] perInch = { 1.0, 2.54, 25.4, 96.0, 72.0, 6.0 };
    for (int i = 0; i < units.length; i++) {
      for (int j = 0; j < units.length; j++) {
        double expected = perInch[j] / perInch[i];
        assertEquals(factor(units[i], units[j]), expected, 1e-9,
            units[i] + " -> " + units[j]);
        assertEquals(factor(units[j], units[i]), 1.0 / expected, 1e-9,
            units[j] + " -> " + units[i]);
      }
    }
  }

  /**
   * Physical round-trips: converting a value to another length unit and back
   * returns the original value. The length matrix anchors each factor; this
   * confirms the composition of both directions numerically.
   */
  @Test
  public void testRoundTrips() {
    Unit[] units = { Unit.IN, Unit.CM, Unit.MM, Unit.PX, Unit.PT, Unit.PC };
    for (Unit from : units) {
      for (Unit to : units) {
        assertEquals(factor(from, to) * factor(to, from), 1.0, 1e-6,
            from + " -> " + to + " -> " + from);
      }
    }
  }

  /**
   * Full resolution-unit matrix: 1dppx = 96dpi = 96/2.54dpcm. Every ordered
   * pair must satisfy factor(a, b) = per-dppx(b) / per-dppx(a), and the
   * inverse must hold in the reverse direction. dppx->dpcm is 96/2.54
   * (~37.795), not 2.54*96 (243.84).
   */
  @Test
  public void testResolutionMatrix() {
    Unit[] units = { Unit.DPPX, Unit.DPI, Unit.DPCM };
    double[] perDppx = { 1.0, 96.0, 96.0 / 2.54 };
    for (int i = 0; i < units.length; i++) {
      for (int j = 0; j < units.length; j++) {
        double expected = perDppx[j] / perDppx[i];
        assertEquals(factor(units[i], units[j]), expected, 1e-9,
            units[i] + " -> " + units[j]);
        assertEquals(factor(units[j], units[i]), 1.0 / expected, 1e-9,
            units[j] + " -> " + units[i]);
      }
    }
  }

  /**
   * Full angular-unit matrix: 1turn = 360deg = 400grad = 2pi rad. Every
   * ordered pair must satisfy factor(a, b) = per-turn(b) / per-turn(a), and
   * the inverse must hold in the reverse direction. deg->grad is the exact
   * reciprocal of grad->deg (10/9), not 9/10.
   */
  @Test
  public void testAngleMatrix() {
    Unit[] units = { Unit.TURN, Unit.DEG, Unit.GRAD, Unit.RAD };
    double[] perTurn = { 1.0, 360.0, 400.0, 2 * Math.PI };
    for (int i = 0; i < units.length; i++) {
      for (int j = 0; j < units.length; j++) {
        double expected = perTurn[j] / perTurn[i];
        assertEquals(factor(units[i], units[j]), expected, 1e-9,
            units[i] + " -> " + units[j]);
        assertEquals(factor(units[j], units[i]), 1.0 / expected, 1e-9,
            units[j] + " -> " + units[i]);
      }
    }
  }

}
