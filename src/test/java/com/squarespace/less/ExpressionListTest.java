package com.squarespace.less;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class ExpressionListTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.EXPRESSION_LIST);

    h.parseEquals("1, 2, 3", expnlist(dim(1), dim(2), dim(3)));
    h.parseEquals("a b, c d", expnlist(expn(kwd("a"), kwd("b")), expn(kwd("c"), kwd("d"))));
  }

}
