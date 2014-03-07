package com.squarespace.less;

import static com.squarespace.less.model.Unit.PX;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.LessStream;
import com.squarespace.less.parse.Parselets;


public class RuleTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule rule_1 = rule(prop("color"), color("#123"));
    Rule rule_2 = rule(prop("color"), color("#123"), true);

    assertEquals(rule(prop("color"), color("#123")), rule(prop("color"), color("#123")));

    assertNotEquals(rule_1, null);
    assertNotEquals(rule_1, anon("#foo"));
    assertNotEquals(rule_1, rule_2);
  }

  @Test
  public void testModelReprSafety() {
    rule(prop("font-size"), dim(12, Unit.PX), true).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);

    h.parseEquals("foo  :   #123;", rule(prop("foo"), color("#123")));
    h.parseEquals("foo: 12px", rule(prop("foo"), dim(12, Unit.PX)));

    h.parseEquals("foo \t : \t /* x */ 1 /* y */ 2",
        rule(prop("foo"), expn(comment(" x ", true), dim(1), comment(" y ", true), dim(2))));

    h.parseEquals("foo: foo: foo: 123   ;", rule(prop("foo"), anon("foo: foo: 123")));

    h.parseEquals("font:italic bold 12px/30px Georgia, serif", rule(prop("font"), expnlist(
        expn(kwd("italic"), kwd("bold"), shorthand(dim(12, PX), dim(30, PX)), kwd("Georgia")), kwd("serif"))));

    h = new LessHarness(Parselets.STYLESHEET);

    Stylesheet exp = stylesheet();
    exp.add(rule(prop("foo"), anon("")));
    exp.add(rule(prop("bar"), color("#123")));
    h.parseEquals("foo: ;\nbar: #123;", exp);
  }

  @Test
  public void testAnonRuleValueParse() throws LessException {
    LessStream stm = new LessStream("foo bar;");
    assertTrue(stm.matchAnonRuleValue());
    assertEquals(stm.token(), "foo bar");

    stm = new LessStream("foo: foo: foo: 123 ;");
    assertTrue(stm.matchAnonRuleValue());
    assertEquals(stm.token(), "foo: foo: foo: 123 "); // token before trimming
}

}
