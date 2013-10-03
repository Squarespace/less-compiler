package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.CHILD;
import static com.squarespace.v6.template.less.model.Operator.EQUAL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Guard;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.model.Unit;


public class MixinTest extends LessTestBase {
  
  @Test
  public void testEquals() {
    MixinParams params = params(param("@a"), param("@b", anon("c")));
    Guard guard = guard(cond(EQUAL, var("@a"), anon("b")));

    assertEquals(mixin(".foo"), mixin(".foo"));
    assertEquals(mixin(".foo", params, guard), mixin(".foo", params, guard));
    
    assertNotEquals(mixin(".foo"), null);
    assertNotEquals(mixin(".foo"), mixin(".bar"));
    assertNotEquals(mixin(".foo"), mixin(".foo", params, guard));
    assertNotEquals(mixin(".foo", params, guard), mixin(".foo"));
    assertNotEquals(mixin(".foo", params, null), mixin(".foo", params, guard));
  }
  
  @Test
  public void testModelReprSafety() {
    MixinParams params = params(param("@a"), param("@b", anon("c")));
    Guard guard = guard(cond(EQUAL, var("@a"), anon("b")));

    mixin(".foo").toString();
    mixin("#ns", params, guard).toString();
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness();
    
    Node res = h.parse(".stringguard(@str) when not (\"theme2\" = @str) { content: is not theme2; }");
    System.out.println(res);
  }

  @Test
  public void testSimple() throws LessException {
    Ruleset r2 = ruleset(selector(element("span")));
    r2.add(rule(prop("color"), color("#f00")));
    r2.add(rule(prop("font-size"), dim(12, Unit.PX)));
    
    Ruleset r1 = ruleset(selector(element(".one")));
    r1.add(r2);
    
    Ruleset r0 = ruleset(selector(element("#ns")));
    r0.add(r1);
    
    MixinCall m0 = new MixinCall(selector(element("#ns"), element(CHILD, ".one")), null, false);
    
    Stylesheet root = sheet();
    root.add(r0);
    root.add(m0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }
}
