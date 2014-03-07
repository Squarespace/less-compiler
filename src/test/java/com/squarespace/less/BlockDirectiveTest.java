package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.Parselets;


public class BlockDirectiveTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule ruleXY = rule(prop("x"), anon("y"));
    Rule ruleXZ = rule(prop("x"), anon("z"));
    Block blockXY = block(ruleXY);
    Block blockXZ = block(ruleXZ);

    assertEquals(dir("x", blockXY), dir("x", blockXY));

    assertNotEquals(dir("x", blockXY), dir("y", blockXY));
    assertNotEquals(dir("x", blockXZ), dir("x", blockXY));
  }

  @Test
  public void testModelReprSafety() {
    dir("x", block(rule(prop("x"), anon("y")))).toString();
  }

  @Test
  public void testBlockDirective() throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);

    Stylesheet sheet = stylesheet();
    sheet.add(dir("@page foo", block(rule(prop("a"), dim(1)))));
    h.parseEquals("@page foo { a: 1; }", sheet);

    sheet = stylesheet();
    sheet.add(dir("@namespace", expn(kwd("foo"), kwd("bar"))));
    h.parseEquals("@namespace foo bar;", sheet);
  }

}
