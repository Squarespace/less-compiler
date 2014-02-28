package com.squarespace.v6.template.less.plugins;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class GeneralFunctionsTest extends LessTestBase {

  @Test
  public void testFunctions() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);
    
    h.evalEquals("e('foo')", anon("foo"));
    h.evalEquals("e('-moz-foo-bar')", anon("-moz-foo-bar"));
    
    // Escape
    h.evalEquals("escape(' (hi) ')", anon("%20%28hi%29%20"));
    
    // Format
    h.evalEquals("%('%s, %s', 12px, 'foo')", quoted('\'', false, anon("12px, foo")));
    h.evalEquals("%('%s %A', '()', #ff1133)", quoted('\'', false, anon("() %23f13")));
    h.evalEquals("%('%s %A', '()', #f00)", quoted('\'', false, anon("() %23f00")));
  }
  
}
