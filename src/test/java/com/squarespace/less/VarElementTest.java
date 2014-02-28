package com.squarespace.less;

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.model.Combinator.DESC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class VarElementTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(varelem(null, var("@a")), varelem(null, var("@a")));

    assertNotEquals(varelem(null, var("@a")), null);
    assertNotEquals(varelem(null, var("@a")), anon("foo"));
    assertNotEquals(varelem(null, var("@a")), varelem(CHILD, var("@a")));
    assertNotEquals(varelem(DESC, var("@a")), varelem(CHILD, var("@a")));
    assertNotEquals(varelem(DESC, var("@a")), varelem(DESC, var("@b")));
  }

  @Test
  public void testModelReprSafety() {
    varelem(CHILD, var("@foo")).toString();
  }

}

