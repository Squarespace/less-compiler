package com.squarespace.less.core;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;


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
