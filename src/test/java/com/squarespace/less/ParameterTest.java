package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class ParameterTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(param("@x"), param("@x"));
    assertEquals(param("@x", true), param("@x", true));
    assertEquals(param("@x", anon("y")), param("@x", anon("y")));

    assertNotEquals(param("@x"), null);
    assertNotEquals(param("@x"), param("@y"));
    assertNotEquals(param("@x"), param("@x", true));
    assertNotEquals(param("@x", anon("y")), param("@x", true));
    assertNotEquals(param("@x", anon("y")), param("@x", anon("z")));
  }

  @Test
  public void testModelReprSafety() {
    param("@x").toString();
    param("@x", anon("y")).toString();
    param("@x", true).toString();
  }

}
