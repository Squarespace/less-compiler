package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Parameter;


public class ParametersTest extends LessTestBase {

  @Test
  public void testEquals() {
    Parameter paramXY = param("@x", anon("y"));
    Parameter paramX = param("@x", true);

    assertEquals(params(), params());
    assertEquals(params(paramXY, paramX), params(paramXY, paramX));

    assertNotEquals(params(paramXY, paramX), null);
    assertNotEquals(params(paramXY, paramX), params(paramXY));
    assertNotEquals(paramXY, paramX);
  }

  @Test
  public void testModelReprSafety() {
    params(param("@x", anon("y")), param("@z", true)).toString();
  }

}
