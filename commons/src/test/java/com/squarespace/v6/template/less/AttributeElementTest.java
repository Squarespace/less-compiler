package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.CHILD;
import static com.squarespace.v6.template.less.model.Combinator.DESC;
import static com.squarespace.v6.template.less.model.Combinator.SIB_ADJ;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.parse.Parselets;


public class AttributeElementTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(attr(DESC, "x"), attr(DESC, "x"));
    assertEquals(attr(null, "y", anon("z")), attr(null, "y", anon("z")));

    assertNotEquals(attr(null, "y", anon("z")), attr(null, anon("y")));
    assertNotEquals(attr(null, "y", anon("z")), attr(DESC, "y", anon("z")));
  }
  
  @Test
  public void testModelReprSafety() {
    attr(DESC, "x", "y").toString();
    attr(null, "y").toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.ELEMENT);
    
    h.parseEquals(" [class]", attr(DESC, anon("class")));
    h.parseEquals(">[foo=\"bar\"]", attr(CHILD, anon("foo"), anon("="), quoted('"', false, "bar")));
    h.parseEquals("+[a=\"@{bar}\"]", attr(SIB_ADJ, anon("a"), anon("="), quoted('"', false, var("@bar", true))));
  }
  
}
