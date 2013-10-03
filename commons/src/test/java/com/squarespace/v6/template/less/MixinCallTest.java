package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.Selector;


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
  public void testMixinCall() throws LessException {
    LessHarness h = new LessHarness();
    String raw = "@a: 12px;"
        + ".x(@b: #123) { color: @b; font-size: @a }"
        + ".x() { foo: bar; }"
        + ".x { a: 1 }"
        + ".y { .x(#456); }"
        + ".z { .x() }";
//        + ".q { .x(#456, #789); }";
//    raw = "//.pre { #ns > .mixin; }\n #ns { @a: def-scope; .mixin () { a: @a; b: @b; } }  .post1 { @b: call-scope; #ns > .mixin; }";
//    raw = ".mixin(@a) when (@a<=1) { a: @a } .x { .mixin(2); } .y { .mixin(1); } .z { .mixin(-1); }";
//    raw = ".bg() { c: red; @media foo { c: green; } } .y { .bg(); }";

    raw = ".mediaMixin(@fallback: 200px) { background: black;  @media handheld { background: white;  "
        + "@media (max-width: @fallback) { background: red; } } }  .a { .mediaMixin(100px); }  .b { .mediaMixin(); }";
    
    String res = h.execute(raw);
    System.out.println(res);
  }
  
}
