package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class QuotedTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(quoted('"', "foo", "bar"), quoted('"', "foo", "bar"));
    assertEquals(quoted('"', var("@foo"), "bar"), quoted('"', var("@foo"), "bar"));

    assertNotEquals(quoted('"', "foo", "bar"), null);
    assertNotEquals(quoted('"', "foo", "bar"), anon("foobar"));
    assertNotEquals(quoted('"', "foo", "bar"), quoted('\'', "foo", "bar"));
    assertNotEquals(quoted('"', "foo", "bar"), quoted('"', "bar"));
  }
  
  @Test
  public void testModelReprSafety() {
    quoted('"', "foo").toString();
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.QUOTED);
    
    h.parseEquals("'single'", quoted('\'', "single"));
    h.parseEquals("\"double\"", quoted('"', "double"));
    h.parseEquals("\"'foo'\"", quoted('"', "'foo'"));
    h.parseEquals("'\"foo\"'", quoted('\'', "\"foo\""));
    h.parseEquals("'\\\\'", quoted('\'', "\\\\"));
    
    h.parseEquals("'@{a} . @{b}'", quoted('\'', var("@a", true), " . ", var("@b", true)));
    h.parseEquals("'@ {a}'", quoted('\'', "@ {a}"));
    h.parseEquals("\"@{ x }\"", quoted('"', "@{ x }"));
    h.parseEquals("\" @{\"", quoted('"', " @{"));
  }
  
}
