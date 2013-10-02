package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.parse.Parselets.STYLESHEET;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.model.Unit;


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
    LessHarness h = new LessHarness(STYLESHEET);
    
    Stylesheet exp = stylesheet();
    exp.add(rule(prop("foo"), color("#123")));
    h.parseEquals("foo  :   #123;", exp);

    exp = stylesheet();
    exp.add(rule(prop("foo"), anon("")));
    exp.add(rule(prop("bar"), color("#123")));
    h.parseEquals("foo: ;\nbar: #123;", exp);
  }
  
}
