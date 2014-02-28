package com.squarespace.v6.template.less.plugins;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


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
