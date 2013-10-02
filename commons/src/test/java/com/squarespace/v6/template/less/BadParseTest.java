package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.SyntaxErrorType.INCOMPLETE_PARSE;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.parse.Parselets;


public class BadParseTest {

  @Test
  public void testBad() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);

    h.parseFails(".foo { { }", INCOMPLETE_PARSE);
    h.parseFails("@", INCOMPLETE_PARSE);
  }

}
