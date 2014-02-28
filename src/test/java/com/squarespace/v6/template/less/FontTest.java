package com.squarespace.v6.template.less;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class FontTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.FONT);
    h.parseEquals("0/0 a", expnlist(expn(ratio("0/0"), kwd("a"))));
    h.parseEquals("small/0 a", expnlist(expn(shorthand(kwd("small"), dim(0)), kwd("a"))));
  }
  
}
