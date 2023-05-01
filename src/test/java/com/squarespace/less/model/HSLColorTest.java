package com.squarespace.less.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HSLColorTest {

  @Test
  public void testBasic() {
    Assert.assertThrows(() -> new HSLColor(-2.0, 0.5, 0.5));
  }
}
