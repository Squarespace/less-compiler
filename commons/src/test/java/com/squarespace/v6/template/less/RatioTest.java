package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;


public class RatioTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(ratio("3/4"), ratio("3/4"));

    assertNotEquals(ratio("3/4"), null);
    assertNotEquals(ratio("3/4"), anon("foo"));
    assertNotEquals(ratio("3/4"), ratio("2/3"));
  }
  
  @Test
  public void testModelReprSafety() {
    ratio("3/4").toString();
  }
  
}
