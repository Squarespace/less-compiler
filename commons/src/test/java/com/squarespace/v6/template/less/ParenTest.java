package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;


public class ParenTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(paren(anon("x")), paren(anon("x")));

    assertNotEquals(paren(dim(1)), null);
    assertNotEquals(paren(dim(1)), anon("a"));
    assertNotEquals(paren(dim(1)), paren(dim(2)));
  }
  
  @Test
  public void testModelReprSafety() {
    paren(anon("a")).toString();
  }
  
}
