package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.DESC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class SelectorsTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(selectors(selector(element("h1"))), selectors(selector(element("h1"))));

    assertNotEquals(selectors(selector(element("h1"))), null);
    assertNotEquals(selectors(selector(element("h1"))), selector(element("foo")));
    assertNotEquals(selectors(selector(element("h1"))), selectors(selector(element("h2"))));
  }
  
  @Test
  public void testModelReprSafety() {
    selectors(selector(element("a")), selector(element("y"), element("z"))).toString();
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.SELECTORS);
    
    h.parseEquals("a, b", 
        selectors(selector(element(DESC, "a")), selector(element(DESC, "b"))));
  }
  
}
