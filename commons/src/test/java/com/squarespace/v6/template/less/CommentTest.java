package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class CommentTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(comment("x", true), comment("x", true));
    assertEquals(comment("y", false), comment("y", false));
    assertNotEquals(comment("x", false), comment("y", false));
    assertNotEquals(comment("x", false), comment("x", true));
  }
  
  @Test
  public void testModelReprSafety() {
    comment("x", false).toString();
  }
  
  @Test
  public void testComment() throws LessException {
    LessHarness h = new LessHarness(Parselets.COMMENT);
    h.parseEquals("/*** a*b/c ***/", comment("** a*b/c **", true));
    h.parseEquals("/* foo */", comment(" foo ", true));
    h.parseEquals("// foo", comment(" foo", false));
    h.parseEquals("// a //", comment(" a //", false));
  }

}
