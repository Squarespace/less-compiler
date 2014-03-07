package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Stylesheet;


public class SylesheetTest extends LessTestBase {

  @Test
  public void testEquals() {
    Stylesheet sheetXY = stylesheet();
    sheetXY.add(rule(prop("x"), anon("y")));
    sheetXY.add(rule(prop("y"), anon("z")));

    Stylesheet sheetZZ = stylesheet();
    sheetZZ.add(rule(prop("z"), anon("z")));

    assertEquals(stylesheet(), stylesheet());
    assertEquals(sheetXY, sheetXY);

    assertNotEquals(sheetXY, null);
    assertNotEquals(sheetXY, stylesheet());
    assertNotEquals(sheetXY, prop("foo"));
    assertNotEquals(sheetXY, sheetZZ);
  }

  @Test
  public void testModelReprSafety() {
    Stylesheet sheet = stylesheet();
    sheet.add(rule(prop("foo"), anon("bar")));
    sheet.toString();
  }

}
