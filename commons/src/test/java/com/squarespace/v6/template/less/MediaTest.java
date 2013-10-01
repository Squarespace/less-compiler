package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.exec.LessEngine;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class MediaTest extends LessTestBase {

  /*
  
  @Test
  public void testEquals() {
    Features feat_ab = features(expn(kwd("a")), expn(kwd("b")));
    Features feat_ac = features(expn(kwd("a")), expn(kwd("c")));
    
    assertEquals(media(feat_ab), media(feat_ab));
    assertNotEquals(media(feat_ab), media(feat_ac));
  }
  
  @Test
  public void testParse() throws LessException {
    List<String> strings = Arrays.asList(
        "@media a, b { .one { color: #123; } @media c { .two { font-size: 12px; } } }",
        "@media a { @media b { .one { color: #fff; }}}",
        "@foo: 12px; @media a, (b: @foo) { .one { color: #000; } }",
        "@bar: @baz; @baz: '#123'; .one { color: color(@bar); }",
        
        // test-case-1
        "body { @media print { padding: 20px;  header { background-color: red; }  "
          + "@media (orientation:landscape) { margin-left: 20px; } } }",
        
        // test-case-2
        "@var: 42;  @media print { .class { color: blue; .sub { width: @var; } } "
          + ".top, header > h1 { color: #222 * 2; } }",
        
        // test-case-4
        ".v { .w { a: 1; .x { b: 2 } } .y { c: 3; .z { d: 4; } } }",
        
        // test-case-5
        "@media print { .x { a: 1; .y { b: 2 } @media bar { .z { @media screen { c: 3 } } } } }",
        
        // test-case-6
        "@media x { @page y { a: 1cm; } @page z { b: 8.5in 11in;  @top-left { c: 12in; @page zz { d: 10in; } } } }",
                
        ".x() { color: #fff; } .y { .x; .z; } .z { font-size: 8px; }",
        
        // missing mixin
//        ".x { .z; color: #fff; }"
        
        // recursive ruleset mixin
        ".foo { .x { color: #fff; .foo; } }"
    );
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    for (String str : strings) {
      LessEngine engine = new LessEngine();
      Node result = h.parse(str);
      try {
        System.out.println(engine.render((Stylesheet)result, h.context()));
      } catch (LessException e) {
        System.out.println(e.getMessage());
      }
      System.out.println("------------------------------------");
    }
  }
  
  @Test
  public void testRender() throws LessException {
    LessHarness h = new LessHarness(Parselets.DIRECTIVE);

    Ruleset ruleset = ruleset(selector(element(".one")));
    ruleset.add(rule(prop("color"), color("#123")));
    
    Media media1 = media(features(expn(kwd("a")), expn(kwd("b"))));
    media1.add(ruleset);
    media1.add(rule(prop("background"), color("#000")));
    
    ruleset = ruleset(selector(element(".two")));
    ruleset.add(rule(prop("font-size"), var("@foo")));
    
    Media media2 = media(features(expn(kwd("c")), expn(paren(rule(prop("foo"), var("@foo"))))));
    media2.add(ruleset);
    media1.add(media2);

    Stylesheet sheet = stylesheet();
    sheet.add(media1);
    sheet.add(def("@foo", dim(12, Unit.PX)));
    
//    CssRenderer cr = new CssRenderer(h.context(new Options(false)));
//    System.out.println(cr.render(sheet));
  }
  */
  
}
