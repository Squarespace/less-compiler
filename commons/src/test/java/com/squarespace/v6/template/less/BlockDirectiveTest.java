package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Rule;


public class BlockDirectiveTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule rule_xy = rule(prop("x"), anon("y"));
    Rule rule_xz = rule(prop("x"), anon("z"));
    Block block_xy = block(rule_xy);
    Block block_xz = block(rule_xz);

    assertEquals(dir("x", block_xy), dir("x", block_xy));
    
    assertNotEquals(dir("x", block_xy), dir("y", block_xy));
    assertNotEquals(dir("x", block_xz), dir("x", block_xy));
  }

  @Test
  public void testModelReprSafety() {
    dir("x", block(rule(prop("x"), anon("y")))).toString();
  }
  
}
