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
  
}
