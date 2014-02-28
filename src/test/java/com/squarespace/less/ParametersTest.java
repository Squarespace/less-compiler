package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Parameter;


public class ParametersTest extends LessTestBase {

  @Test
  public void testEquals() {
    Parameter param_xy = param("@x", anon("y"));
    Parameter param_v = param("@x", true);

    assertEquals(params(), params());
    assertEquals(params(param_xy, param_v), params(param_xy, param_v));
    
    assertNotEquals(params(param_xy, param_v), null);
    assertNotEquals(params(param_xy, param_v), params(param_xy));
    assertNotEquals(param_xy, param_v);
  }
  
  @Test
  public void testModelReprSafety() {
    params(param("@x", anon("y")), param("@z", true)).toString();
  }
  
}
