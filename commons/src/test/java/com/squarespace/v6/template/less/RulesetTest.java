package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.CHILD;
import static com.squarespace.v6.template.less.parse.Parselets.RULESET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Combinator;
import com.squarespace.v6.template.less.model.Ruleset;


public class RulesetTest extends LessTestBase {

  @Test
  public void testEquals() {
    Ruleset ruleset_xy = ruleset(selector(element(".x"), element(CHILD, ".y")));
    ruleset_xy.add(rule(prop("color"), color("black")));
    
    assertEquals(ruleset(selector(element(".x"))), ruleset(selector(element(".x"))));
    assertEquals(ruleset_xy, ruleset_xy);

    assertNotEquals(ruleset_xy, null);
    assertNotEquals(ruleset_xy, prop("foo"));
  }
  
  @Test
  public void testModelReprSafety() {
    Ruleset ruleset_xy = ruleset(selector(element(".x"), element(CHILD, ".y")));
    ruleset_xy.add(rule(prop("color"), color("black")));
    ruleset_xy.toString();
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(RULESET);
    
    Ruleset exp = ruleset(selector(element(Combinator.CHILD, ".x")));
    exp.add(rule(prop("a"), anon("#fff")));
    h.parseEquals("> .x { a: #fff; }", exp);
    
    h.parseEquals(".foo { }", ruleset(selector(element(".foo"))));
    
    exp = ruleset(selector(element("span"), attr(null, anon("class"))));
    h.parseEquals("span[class] { }", exp);
    
    // Unicode character in selector
    exp = ruleset(selector(element(".♫")));
    exp.add(rule(prop("length"), anon("52s")));
    h.parseEquals(".♫ { length: 52s; }", exp);
  }
  
}
