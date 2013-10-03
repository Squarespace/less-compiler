package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.DESC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;


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

}
