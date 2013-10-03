package com.squarespace.v6.template.less;

import static org.testng.Assert.fail;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.parse.Parselets;


public class BadParseTest {

  @Test
  public void testBad() throws LessException {
    String[] strings = new String[] {
      ".foo { { }",
      "@"
    };
    
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    
    for (String str : strings) {
      try {
        System.out.println(h.parse(str));
        fail("Expected exception while parsing \"" + StringEscapeUtils.escapeJava(str) + '"');
      } catch (LessException e) {
      }
    }
  }

}
