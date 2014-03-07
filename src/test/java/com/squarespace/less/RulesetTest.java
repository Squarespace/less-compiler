package com.squarespace.less;

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.parse.Parselets.RULESET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Unit;


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
    exp.add(rule(prop("a"), color("#fff")));
    h.parseEquals("> .x { a: #fff; }", exp);

    h.parseEquals(".foo { }", ruleset(selector(element(".foo"))));

    exp = ruleset(selector(element("span"), attr(null, anon("class"))));
    h.parseEquals("span[class] { }", exp);

    // Unicode character in selector
    exp = ruleset(selector(element(".♫")));
    exp.add(rule(prop("length"), dim(52, Unit.S)));
    h.parseEquals(".♫ { length: 52s; }", exp);
  }

}
