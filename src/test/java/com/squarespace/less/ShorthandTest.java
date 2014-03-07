package com.squarespace.less;

import static com.squarespace.less.parse.Parselets.SHORTHAND;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Unit;


public class ShorthandTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(shorthand(dim(1), dim(2)), shorthand(dim(1), dim(2)));

    assertNotEquals(shorthand(dim(1), dim(2)), null);
    assertNotEquals(shorthand(dim(1), dim(2)), dim(2));
    assertNotEquals(shorthand(dim(1), dim(2)), shorthand(dim(3), dim(4)));
  }

  @Test
  public void testModelReprSafety() {
    shorthand(dim(1), dim(2, Unit.PX)).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(SHORTHAND);

    h.parseEquals("small/12px", shorthand(kwd("small"), dim(12, Unit.PX)));
    h.parseEquals("15vm/23vm", shorthand(dim(15, Unit.VM), dim(23, Unit.VM)));
    h.parseEquals("foo/3.14", shorthand(kwd("foo"), dim(3.14)));
    h.parseEquals("@a/@b", shorthand(var("@a"), var("@b")));
  }

}
