package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.parse.Parselets;


public class BlockDirectiveTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule rule_xy = rule(prop("x"), anon("y"));
    Rule rule_xz = rule(prop("x"), anon("z"));
    Block block_xy = block(rule_xy);
    Block block_xz = block(rule_xz);

    assertEquals(dir("x", block_xy), dir("x", block_xy));
    
    assertNotEquals(dir("x", block_xy), dir("y", block_xy));
    assertNotEquals(dir("x", block_xz), dir("x", block_xy));
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
