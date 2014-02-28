package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class ArgumentTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(arg(anon("y")), arg(null, anon("y")));
    assertEquals(arg("x", anon("y")), arg("x", anon("y")));

    assertNotEquals(arg(anon("y")), null);
    assertNotEquals(arg("x", anon("z")), arg("x", anon("y")));
    assertNotEquals(arg("x", anon("y")), arg("x", anon("z")));
  }
  
  @Test
  public void testModelReprSafety() {
    arg(null, anon("y")).toString();
    arg("x", anon("y")).toString();
  }

}
