package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.HSLColor;
import com.squarespace.v6.template.less.model.RGBColor;
import com.squarespace.v6.template.less.parse.Parselets;


public class ColorTest extends LessTestBase {

  @Test
  public void testColor() throws LessException {
    // Conversion of of certain keywords into colors during parse.
    LessHarness h = new LessHarness(Parselets.COLOR_KEYWORD);
    h.parseEquals("blue", rgb(0x00, 0x00, 0xff, 1.0, true));
    h.parseEquals("red", rgb(0xff, 0x00, 0x00, 1.0, true));
    h.parseEquals("black", rgb(0, 0, 0, 1.0, true));
    h.parseEquals("white", rgb(0, 0, 0, 1.0, true));

    h = new LessHarness(Parselets.COLOR);
    h.parseEquals("#fff", rgb(0xff, 0xff, 0xff));
    h.parseEquals("#000", rgb(0x00, 0x00, 0x00));
    h.parseEquals("#010203", rgb(0x01, 0x02, 0x03));
  }
  
  @Test
  public void testHex() {
    hexEquals("#48f", 0x44, 0x88, 0xff);
    hexEquals("#abc", 0xaa, 0xbb, 0xcc);
    hexEquals("#1278cd", 0x12, 0x78, 0xcd);
    hexEquals("#ff01fe", 0xff, 0x01, 0xfe);

    // bad hex chars
    hexEquals("#xyz", 0, 0, 0);
    hexEquals("#11zy22", 0x11, 0, 0x22);
    
    // varying number of leading '#' symbols, parse is tolerant
    hexEquals("010203", 0x01, 0x02, 0x03);
    hexEquals("###123", 0x11, 0x22, 0x33);
  }
  
  @Test
  public void testConversions() throws LessException {
    RGBColor rgb = rgb(200, 100, 50);
    HSLColor hsl = rgb.toHSL();
    assertEquals(hsl.toRGB(), rgb);
    
    rgb = rgb(32, 32, 32);
    hsl = rgb.toHSL();
    assertEquals(hsl.toRGB(), rgb);
    
    hsl = new HSLColor(180, .5, .5);
    assertEquals(hsl.toRGB(), new RGBColor(64, 191, 191));
  }
  
  private void hexEquals(String hex, int red, int green, int blue) {
    RGBColor color = RGBColor.fromHex(hex);
    assertEquals(color.red(), red, "red component of " + hex);
    assertEquals(color.green(), green, "green component of " + hex);
    assertEquals(color.blue(), blue, "blue component of " + hex);
  }
  
  @Test
  public void testBadColors() {
    String[] badColors = new String[] {
      "@123", "", ".", "12", "1234", "12345"
    };
    for (String hex : badColors) {
      try {
        RGBColor.fromHex(hex);
        fail("Expected exception while parsing hex color '" + hex + "'");
      } catch (IllegalArgumentException e) {
      }
    }
  }

}
