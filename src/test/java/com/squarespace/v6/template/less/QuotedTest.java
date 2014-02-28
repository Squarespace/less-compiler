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
    assertEquals(quoted('"', false, "foo", "bar"), quoted('"', false, "foo", "bar"));
    assertEquals(quoted('"', false, var("@foo"), "bar"), quoted('"', false, var("@foo"), "bar"));

    assertNotEquals(quoted('"', false, "foo", "bar"), null);
    assertNotEquals(quoted('"', false, "foo", "bar"), anon("foobar"));
    assertNotEquals(quoted('"', false, "foo", "bar"), quoted('\'', false, "foo", "bar"));
    assertNotEquals(quoted('"', false, "foo", "bar"), quoted('"', false, "bar"));
  }
  
  @Test
  public void testModelReprSafety() {
    quoted('"', false, "foo").toString();
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.QUOTED);
    
    h.parseEquals("'single'", quoted('\'', false, "single"));
    h.parseEquals("\"double\"", quoted('"', false, "double"));
    h.parseEquals("\"'foo'\"", quoted('"', false, "'foo'"));
    h.parseEquals("'\"foo\"'", quoted('\'', false, "\"foo\""));
    h.parseEquals("'\\\\'", quoted('\'', false, "\\\\"));
    
    h.parseEquals("'@{a} . @{b}'", quoted('\'', false, var("@a", true), " . ", var("@b", true)));
    h.parseEquals("'@ {a}'", quoted('\'', false, "@ {a}"));
    h.parseEquals("\"@{ x }\"", quoted('"', false, "@{ x }"));
    h.parseEquals("\" @{\"", quoted('"', false, " @{"));
    
    h.parseEquals("~'foo'", quoted('\'', true, anon("foo")));
    h.parseEquals("~\"@@{var}\"", quoted('"', true, var("@@var", true)));
  }
  
}
