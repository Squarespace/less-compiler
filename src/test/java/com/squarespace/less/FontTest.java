package com.squarespace.less;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class FontTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.FONT);
    h.parseEquals("0/0 a", expnlist(expn(ratio("0/0"), kwd("a"))));
    h.parseEquals("small/0 a", expnlist(expn(shorthand(kwd("small"), dim(0)), kwd("a"))));
  }
  
}
