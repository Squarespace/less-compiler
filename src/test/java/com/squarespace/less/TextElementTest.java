package com.squarespace.less;

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.model.Combinator.DESC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class TextElementTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(element(".x"), element(".x"));
    assertEquals(element(null, ".x"), element(null, ".x"));
    assertEquals(element(CHILD, ".x"), element(CHILD, ".x"));

    assertNotEquals(element(".x"), null);
    assertNotEquals(element(".x"), kwd("foo"));
    assertNotEquals(element(".x"), element(".y"));
    assertNotEquals(element(CHILD, ".x"), element(DESC, ".y"));
    assertNotEquals(element(null, ".x"), element(DESC, ".x"));
  }

  @Test
  public void testModelReprSafety() {
    element(null, ".foo").toString();
    element(CHILD, ".foo").toString();
  }

}
