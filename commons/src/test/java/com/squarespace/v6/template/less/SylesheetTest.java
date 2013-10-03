package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Stylesheet;


public class SylesheetTest extends LessTestBase {

  @Test
  public void testEquals() {
    Stylesheet sheet_xy = sheet();
    sheet_xy.add(rule(prop("x"), anon("y")));
    sheet_xy.add(rule(prop("y"), anon("z")));

    Stylesheet sheet_zz = sheet();
    sheet_zz.add(rule(prop("z"), anon("z")));
    
    assertEquals(sheet(), sheet());
    assertEquals(sheet_xy, sheet_xy);
    
    assertNotEquals(sheet_xy, null);
    assertNotEquals(sheet_xy, sheet());
    assertNotEquals(sheet_xy, prop("foo"));
    assertNotEquals(sheet_xy, sheet_zz);
  }
  
  @Test
  public void testModelReprSafety() {
    Stylesheet sheet = sheet();
    sheet.add(rule(prop("foo"), anon("bar")));
    sheet.toString();
  }
  
}
