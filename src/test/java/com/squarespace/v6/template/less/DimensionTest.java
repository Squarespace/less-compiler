package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class DimensionTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(dim(1), dim(1));
    assertEquals(dim(1, Unit.PX), dim(1, Unit.PX));
    assertEquals(dim(3.5, Unit.PX), dim(3.5, Unit.PX));

    assertNotEquals(dim(1), dim(2));
    assertNotEquals(dim(3.5), dim(5.3));
    assertNotEquals(dim(1), dim(1, Unit.PX));
    assertNotEquals(dim(1, Unit.IN), dim(1, Unit.PX));
    assertNotEquals(dim(3.5, Unit.IN), dim(3.5, Unit.PX));
    assertNotEquals(dim(3.5, Unit.IN), dim(3.5, Unit.PX));
  }
  
  @Test
  public void testModelReprSafety() {
    dim(1).toString();
    dim(1, Unit.PX).toString();
    dim(3.5, Unit.IN).toString();
  }
  
  @Test
  public void testDimension() throws LessException {
    LessHarness h = new LessHarness(Parselets.DIMENSION);

    h.parseEquals("0", dim(0));
    h.parseEquals("1.34", dim(1.34));
    h.parseEquals(".701", dim(0.701));
    h.parseEquals("0.7011", dim(0.7011));
    h.parseEquals("10px", dim(10, Unit.PX));
    h.parseEquals("30.1dpi", dim(30.1, Unit.DPI));
  }

}
