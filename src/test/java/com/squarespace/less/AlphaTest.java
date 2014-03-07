package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


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

    h.parseEquals("alpha(opacity=12)", alpha(dim(12)));
    h.parseEquals("alpha(opacity=@foo)", alpha(var("@foo")));
    h.parseEquals("alpha(opacity=)", alpha(""));
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    h.parseEquals("alpha(opacity=)", alpha(anon("")));
    h.parseEquals("alpha(opacity=1)", alpha(dim(1)));
    h.parseEquals("alpha(opacity=@var)", alpha(var("@var")));
  }

}
