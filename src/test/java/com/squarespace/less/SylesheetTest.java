package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Stylesheet;


public class SylesheetTest extends LessTestBase {

  @Test
  public void testEquals() {
    Stylesheet sheet_xy = stylesheet();
    sheet_xy.add(rule(prop("x"), anon("y")));
    sheet_xy.add(rule(prop("y"), anon("z")));

    Stylesheet sheet_zz = stylesheet();
    sheet_zz.add(rule(prop("z"), anon("z")));

    assertEquals(stylesheet(), stylesheet());
    assertEquals(sheet_xy, sheet_xy);

    assertNotEquals(sheet_xy, null);
    assertNotEquals(sheet_xy, stylesheet());
    assertNotEquals(sheet_xy, prop("foo"));
    assertNotEquals(sheet_xy, sheet_zz);
  }

  @Test
  public void testModelReprSafety() {
    Stylesheet sheet = stylesheet();
    sheet.add(rule(prop("foo"), anon("bar")));
    sheet.toString();
  }

}
