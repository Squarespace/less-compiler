package com.squarespace.less;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;

public class KeywordTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.KEYWORD);

    h.parseEquals("minControlsSize", kwd("minControlsSize"));
  }
}
