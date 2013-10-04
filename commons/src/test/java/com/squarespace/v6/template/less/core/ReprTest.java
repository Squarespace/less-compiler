package com.squarespace.v6.template.less.core;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;


public class ReprTest extends LessTestBase {

  @Test
  public void testQuoted() throws LessException {
    Buffer buf = new Buffer(2);
    
    quoted('\'', false, anon("foo"), var("@foo", true)).repr(buf);
    assertEquals(buf.toString(), "'foo@{foo}'");

    buf.reset();
    quoted('"', true, var("@@foo", true)).repr(buf);
    assertEquals(buf.toString(), "~\"@@{foo}\"");
  }
  
}
