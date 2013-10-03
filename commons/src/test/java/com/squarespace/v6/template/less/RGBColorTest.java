package com.squarespace.v6.template.less;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;


public class RGBColorTest extends LessTestBase {

  @Test
  public void testEquals() {
    Assert.assertEquals(rgb(32, 32, 32), rgb(32, 32, 32));
    
    Assert.assertNotEquals(rgb(32, 32, 32), null);
    Assert.assertNotEquals(rgb(32, 32, 32), anon("foo"));
    Assert.assertNotEquals(rgb(32, 32, 32), rgb(1, 32, 32));
    Assert.assertNotEquals(rgb(32, 32, 32), rgb(32, 32, 32, 0.5));
  }
  
  @Test
  public void testModelReprSafety() {
    rgb(32, 32, 32, .7).toString();
  }
  
}
