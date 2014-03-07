package com.squarespace.less;

import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.parse.Parselets;


public class BadParseTest {

  @Test
  public void testBad() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);

    h.parseFails(".foo { { }", INCOMPLETE_PARSE);
    h.parseFails("@", INCOMPLETE_PARSE);
  }

}
