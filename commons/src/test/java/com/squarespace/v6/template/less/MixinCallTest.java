package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.parse.Parselets;


public class MixinCallTest extends LessTestBase {

  @Test
  public void testEquals() {
    Selector sel_xy = selector(element(".x"), element(".y"));
    Selector sel_xz = selector(element(".x"), element(".z"));
    MixinCallArgs args = args(';', arg("@a", anon("b")));
    
    assertEquals(mixincall(sel_xy), mixincall(sel_xy));
    assertEquals(mixincall(sel_xy, args), mixincall(sel_xy, args));
    assertEquals(mixincall(sel_xy, args, true), mixincall(sel_xy, args, true));

    assertNotEquals(mixincall(sel_xy), null);
    assertNotEquals(mixincall(sel_xy), mixincall(sel_xz));
    assertNotEquals(mixincall(sel_xy), mixincall(sel_xy, args));
    assertNotEquals(mixincall(sel_xy, args), mixincall(sel_xy, args, true));
  }
  
  @Test
  public void testModelReprSafety() {
    mixincall(selector(element(".x")), args(';', arg("@a", anon("b"))), true).toString();
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.MIXIN_CALL);

    Node exp = mixincall(selector(element(null, ".x")));
    h.parseEquals(".x;", exp);

    exp = mixincall(selector(element(null, ".x")), args(','));
    h.parseEquals(".x();", exp);
    
    exp = mixincall(selector(element(null, ".mixin")), args(',', arg(var("@a")), arg(var("@b"))));
    h.parseEquals(".mixin(@a, @b)", exp);
    
    exp = mixincall(selector(element(null, ".x")), args(',', arg("@a", dim(1))));
    h.parseEquals(".x(@a: 1)", exp);
  }
  
}
