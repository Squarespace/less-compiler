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
import com.squarespace.v6.template.less.model.Combinator;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.parse.Parselets;


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
  public void testRender() throws LessException {
    Selector sel = selector(element("li"), element(null, ".bar"), element(CHILD, "span"), element(null, ".foo"),
          element(DESC, "baz"));

    assertEquals(render(false, sel), "li.bar > span.foo baz");
    assertEquals(render(true, sel), "li.bar>span.foo baz"); 

    sel = selector(element("p"), element(null, ".para"), element(SIB_ADJ, "span"), element(null, ".word"),
        element(DESC, "b"));

    assertEquals(render(false, sel), "p.para + span.word b");
    assertEquals(render(true, sel), "p.para+span.word b");
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTOR);
    
    h.parseEquals("p", selector(element(DESC, "p")));
    h.parseEquals("> p", selector(element(Combinator.CHILD, "p")));
    h.parseEquals("+ p.class", selector(element(Combinator.SIB_ADJ, "p"), element(null, ".class")));
    
    Selector exp = selector(element("p"), attr(null, "class", "~=", quoted('"', false, "a")));
    h.parseEquals("p[class~=\"a\"]", exp);
  }
  
  private String render(boolean compress, Selector selector) throws LessException {
    Context ctx = new Context(new Options(compress));
    ExecEnv env = ctx.newEnv();
    return env.context().render(selector);
  }

}
