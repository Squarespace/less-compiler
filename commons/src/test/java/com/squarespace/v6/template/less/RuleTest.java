package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.parse.Parselets.RULE;
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
    LessHarness h = new LessHarness(RULE);
    
  }
  
  @Test
  public void testParse2() throws LessException {
//    String input = "background: -webkit-gradient(linear, left top, left bottom, from(red), to(blue));";
    LessHarness h = new LessHarness(STYLESHEET);
//    h.parseEquals(input, null);
    
//    h.parseEquals("foo  :   #123;", rule(prop("foo"), color("#123")));
    Stylesheet root = new Stylesheet();
    root.add(rule(prop("foo"), anon("")));
    root.add(rule(prop("bar"), color("#123")));
//    System.out.println(exp);
//    h.parseEquals("foo: ;\nbar: #123;", exp);

    Context ctx = h.context();
//    CssRenderer cr = new CssRenderer(ctx);
//    System.out.println(cr.render(root));
//    System.out.println("-----------------------");
//    System.out.println(cr.render((Stylesheet)h.parse("foo: ;\nbar: #123;")));
  }
  
}
