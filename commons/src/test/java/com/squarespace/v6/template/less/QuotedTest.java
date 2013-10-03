package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;


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
  
}
