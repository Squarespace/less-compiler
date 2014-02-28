package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessTestBase;


public class AnoymousTest extends LessTestBase {

  @Test
  public void testEquals() throws LessException {
    assertEquals(anon(""), anon(""));
    assertEquals(anon("foo"), anon("foo"));
    
    assertNotEquals(anon("foo"), null);
    assertNotEquals(anon("foo"), anon("bar"));
  }
  
  @Test
  public void testModelReprSafety() {
    anon("x").toString();
  }

}
