package com.squarespace.v6.template.less.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class BufferTest {

  @Test
  public void testEscaping() {
    Buffer buf = new Buffer(0, true);
    buf.startDelim('"');
    assertTrue(buf.inEscape());
    buf.append("\"foo\"");
    buf.endDelim();
    assertEquals(buf.toString(), "\"foo\"");
  }
  
  @Test
  public void testNullEscaping() {
    Buffer buf = new Buffer(0, true);
    buf.startDelim(Chars.NULL);
    assertTrue(buf.inEscape());
    buf.append("foo");
    buf.endDelim();
    assertFalse(buf.inEscape());
    assertEquals(buf.toString(), "foo");
  }
  
}
