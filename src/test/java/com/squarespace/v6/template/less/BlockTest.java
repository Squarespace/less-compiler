package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Rule;


public class BlockTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule rule_xy = rule(prop("x"), anon("y"));
    Rule rule_xz = rule(prop("x"), anon("z"));
    assertEquals(block(rule_xy, rule_xz), block(rule_xy, rule_xz));
    
    assertNotEquals(block(rule_xy), block());
    assertNotEquals(block(), block(rule_xy));
    assertNotEquals(block(rule_xy, rule_xz), block(rule_xz, rule_xy));
  }
  
  @Test
  public void testModelReprSafety() {
    block(rule(prop("x"), anon("y"))).toString();
    block(rule(prop("x"), anon("y"))).toString();
  }
  
}
