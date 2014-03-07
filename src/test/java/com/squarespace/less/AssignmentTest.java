package com.squarespace.less;

import static com.squarespace.less.parse.Parselets.ASSIGNMENT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Unit;


public class AssignmentTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(assign("x", anon("y")), assign("x", anon("y")));

    assertNotEquals(assign("x", anon("y")), null);
    assertNotEquals(assign("x", anon("y")), assign("z", anon("y")));
    assertNotEquals(assign("x", anon("y")), assign("x", anon("z")));
  }

  @Test
  public void testModelReprSafety() {
    assign("x", anon("y")).toString();
  }

  @Test
  public void testAssignment() throws LessException {
    LessHarness h = new LessHarness(ASSIGNMENT);
    h.parseEquals("foo=1px", assign("foo", dim(1, Unit.PX)));
    h.parseEquals("foo=@bar", assign("foo", var("@bar")));
  }

}
