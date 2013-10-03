package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class AlphaTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(alpha(anon("foo")), alpha(anon("foo")));

    assertNotEquals(alpha("foo"), null);
    assertNotEquals(alpha("foo"), alpha("bar"));
  }

  @Test
  public void testModelReprSafety() {
    alpha("x").toString();
  }
  
  @Test
  public void testAlpha() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.parseEquals("alpha(opacity=12)", alpha("12"));
    h.parseEquals("alpha(opacity=@foo)", alpha(var("@foo")));
    h.parseEquals("alpha(opacity=)", alpha(""));
  }

}
