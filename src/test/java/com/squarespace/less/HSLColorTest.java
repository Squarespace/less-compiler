package com.squarespace.less;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class HSLColorTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(hsl(0, 0, 1.0), rgb(255, 255, 255).toHSL());
    assertEquals(rgb(255, 255, 255).toHSL().toRGB().toHSL(), hsl(0, 0, 1.0));
  }
  
}
