package com.squarespace.less;

import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class RatioTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(ratio("3/4"), ratio("3/4"));

    assertNotEquals(ratio("3/4"), null);
    assertNotEquals(ratio("3/4"), anon("foo"));
    assertNotEquals(ratio("3/4"), ratio("2/3"));
  }

  @Test
  public void testModelReprSafety() {
    ratio("3/4").toString();
  }

  @Test
  public void testRatio() throws LessException {
    LessHarness h = new LessHarness(Parselets.RATIO);

    h.parseEquals("15/30", ratio("15/30"));
    h.parseFails("foo/bar", INCOMPLETE_PARSE);
  }

}
