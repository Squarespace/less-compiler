package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Rule;


public class BlockTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule ruleXY = rule(prop("x"), anon("y"));
    Rule ruleXZ = rule(prop("x"), anon("z"));
    assertEquals(block(ruleXY, ruleXZ), block(ruleXY, ruleXZ));

    assertNotEquals(block(ruleXY), block());
    assertNotEquals(block(), block(ruleXY));
    assertNotEquals(block(ruleXY, ruleXZ), block(ruleXZ, ruleXY));
  }

  @Test
  public void testModelReprSafety() {
    block(rule(prop("x"), anon("y"))).toString();
    block(rule(prop("x"), anon("y"))).toString();
  }

}
