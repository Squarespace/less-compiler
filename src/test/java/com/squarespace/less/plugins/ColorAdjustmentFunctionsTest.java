package com.squarespace.less.plugins;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class ColorAdjustmentFunctionsTest extends LessTestBase {

  @Test
  public void testFunctions() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);

    // Tests to ensure that the functions in this package basically work.
    // The deeper testing for these functions is moved into the external
    // test suite files (LessTestSuite)

    h.evalEquals("contrast(#fff, #111, #999, .5)", color("#111"));
    h.evalEquals("contrast(#222, #111, #999, .5)", color("#999"));

    h = new LessHarness(Parselets.ADDITION);
    h.evalEquals("darken(#888, 50%) + #000", color("#080808"));
    h.evalEquals("darken(#f00, 40%) + #000", color("#330000"));
    h.evalEquals("darken(#fff, 5) + #000", color("#f2f2f2"));
    h.evalEquals("darken(darken(#fff, 0), 5) + #000", color("#f2f2f2"));
  }

}
