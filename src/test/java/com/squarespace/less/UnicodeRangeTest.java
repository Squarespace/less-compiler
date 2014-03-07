package com.squarespace.less;

import static com.squarespace.less.parse.Parselets.UNICODE_RANGE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;


public class UnicodeRangeTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(unicode("a"), unicode("a"));

    assertNotEquals(unicode("a"), null);
    assertNotEquals(unicode("a"), kwd("a"));
    assertNotEquals(unicode("a"), unicode("b"));
  }

  @Test
  public void testModelReprSafety() {
    unicode("abc").toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(UNICODE_RANGE);

    h.parseEquals("U+?", unicode("U+?"));
    h.parseEquals("U+0-7F", unicode("U+0-7F"));
    h.parseEquals("U+41-5A", unicode("U+41-5A"));
  }

}
