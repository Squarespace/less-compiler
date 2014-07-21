package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class DirectiveTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(dir("@namespace", var("@a")), dir("@namespace", var("@a")));
    assertNotEquals(dir("@namespace", kwd("a")), dir("@namespace", var("@a")));
  }

  @Test
  public void testDirective() throws LessException {
    LessHarness h = new LessHarness(Parselets.DIRECTIVE);

    h.parseEquals("@namespace foo;", dir("@namespace", kwd("foo")));
  }
}
