package com.squarespace.less.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HSLColorTest {

  @Test
  public void testBasic() {
    // Hue is a wheel fraction: negative and out-of-range values wrap
    // into [0, 360) instead of throwing.
    Assert.assertEquals(new HSLColor(0.25, 0.5, 0.5).hue(), 90.0);
    Assert.assertEquals(new HSLColor(-0.25, 0.5, 0.5).hue(), 270.0);
    Assert.assertEquals(new HSLColor(1.5, 0.5, 0.5).hue(), 180.0);
  }

  @Test
  public void testFromHSVAWrapsNegativeHue() {
    // Negative hue must wrap instead of indexing the HSV permutation
    // table out of range. -30 degrees == 330 degrees.
    RGBColor c = RGBColor.fromHSVA(-1.0 / 12.0, 1.0, 1.0, 1.0);
    Assert.assertEquals(c.red(), 255);
    Assert.assertEquals(c.green(), 0);
    Assert.assertEquals(c.blue(), 128);
  }
}
