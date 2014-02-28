package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class PropertyTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(prop("x"), prop("x"));

    assertNotEquals(prop("x"), null);
    assertNotEquals(prop("x"), anon("x"));
    assertNotEquals(prop("x"), prop("y"));
  }
  
  @Test
  public void testModelReprSafety() {
    prop("x").toString();
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.PROPERTY);
    
    h.parseEquals("a", prop("a"));
    h.parseEquals("-moz-foo-bar", prop("-moz-foo-bar"));
    h.parseEquals("*-foo", prop("*-foo"));
    
    h.parseFails("A", INCOMPLETE_PARSE);
  }
  
  
}
