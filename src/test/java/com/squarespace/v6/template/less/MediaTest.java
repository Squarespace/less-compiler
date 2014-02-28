package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.parse.Parselets;


public class MediaTest extends LessTestBase {

  @Test
  public void testEquals() {
    Media m0 = media();
    Media m1 = media(features(kwd("screen")));
    Media m2 = media(features(kwd("mobile")));
    
    assertEquals(m0, media());
    assertEquals(m1, media(features(kwd("screen"))));
    
    assertNotEquals(m0, m1);
    assertNotEquals(m1, m2);
  }
  
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.DIRECTIVE);
    
    Node exp = media(features(expn(kwd("screen"), kwd("and"), kwd("mobile"))));
    h.parseEquals("@media screen and mobile { }", exp);
  }
  
}
