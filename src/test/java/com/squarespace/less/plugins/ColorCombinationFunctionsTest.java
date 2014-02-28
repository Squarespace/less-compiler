package com.squarespace.less.plugins;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class ColorCombinationFunctionsTest extends LessTestBase {

  @Test
  public void testFunctions() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);
    
    // Tests to ensure that the functions in this package basically work.
    // The deeper testing for these functions is moved into the external
    // test suite files (LessTestSuite)

    h.evalEquals("average(#888, #444)", color("#666"));
    h.evalEquals("difference(#888, #444)", color("#444"));
  }
  
}
