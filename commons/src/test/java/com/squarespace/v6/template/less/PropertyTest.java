package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;


public class PropertyTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(prop("x"), prop("x"));

    assertNotEquals(prop("x"), null);
    assertNotEquals(prop("x"), anon("x"));
    assertNotEquals(prop("x"), prop("y"));
  }
  
  @Test
  public void testModelReprSafety() {
    prop("x").toString();
  }
  
}
