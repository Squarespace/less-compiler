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

    assertEquals(factor(Unit.IN, new Unit("quark")), 1.0);
  }

}
