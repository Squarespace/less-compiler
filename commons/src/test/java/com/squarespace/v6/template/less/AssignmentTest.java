package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


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
    LessHarness h = new LessHarness(Parselets.ASSIGNMENT);
    h.parseEquals("foo=1px", assign("foo", dim(1, Unit.PX)));
    h.parseEquals("foo=@bar", assign("foo", var("@bar")));
  }

}
