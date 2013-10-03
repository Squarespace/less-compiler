package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.CHILD;
import static com.squarespace.v6.template.less.model.Combinator.DESC;
import static com.squarespace.v6.template.less.model.Combinator.SIB_ADJ;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Unit;


public class SelectorTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(selector(element(".x")), selector(element(".x")));
    assertEquals(selector(element(".x"), element(".y")), selector(element(".x"), element(".y")));

    assertNotEquals(selector(element(".x")), null);
    assertNotEquals(selector(element(".x")), prop("x"));
    assertNotEquals(selector(element(".x")), selector(element(".y")));
    assertNotEquals(selector(element(DESC, ".x")), selector(element(CHILD, ".x")));
    assertNotEquals(selector(element(null, ".x")), selector(element(CHILD, ".x")));
  }
  
  @Test
  public void testModelReprSafety() {
    selector(element(CHILD, ".foo"), element(".y")).toString();
  }
  
  @Test
  public void testBasic() throws LessException {
    Selector s0 = selector(element("li"), element(null, ".bar"), element(CHILD, "span"), element(null, ".foo"),
          element(DESC, "baz"));
    Selector s1 = selector(element("p"), element(null, ".para"), element(SIB_ADJ, "span"), element(null, ".word"),
        element(DESC, "b"));

    assertEquals(render(false, s0), "li.bar > span.foo baz");
    assertEquals(render(true, s0), "li.bar>span.foo baz"); 

    assertEquals(render(false, s1), "p.para + span.word b");
    assertEquals(render(true, s1), "p.para+span.word b");
  }
  
  @Test
  public void testAttribute() throws LessException {
    GenericBlock defs = defs(def("@bar", dim(12, Unit.PX)));
    Selector s0 = selector(element("p"), attr(null, "class", "~=", quoted('\'', "foo", var("@bar"))));
    
    LessHarness h = new LessHarness(defs);
    Node result = h.evaluate(s0);
    System.out.println(result);
  }

  private String render(boolean compress, Selector selector) throws LessException {
    Context ctx = new Context(new Options(compress));
    ExecEnv env = ctx.newEnv();
    return env.context().render(selector);
  }

}
