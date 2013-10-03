package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.core.Chars.APOSTROPHE;
import static com.squarespace.v6.template.less.core.Chars.QUOTATION_MARK;
import static com.squarespace.v6.template.less.model.Combinator.CHILD;
import static com.squarespace.v6.template.less.model.Combinator.DESC;
import static com.squarespace.v6.template.less.model.Combinator.SIB_ADJ;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Combinator;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Mixin;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Property;
import com.squarespace.v6.template.less.model.Quoted;
import com.squarespace.v6.template.less.model.RGBColor;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.model.Variable;


public class LessTest extends LessTestBase {

  @Test
  public void testBasic() throws LessException {
    Stylesheet root = sheet();
    
    Ruleset r0 = ruleset(selector(element("foo")));
    r0.add(rule(prop("background"), color("#fff")));
    r0.add(rule(prop("font-size"), dim(7, Unit.PX)));
    
    Ruleset r1 = ruleset(selector(element("&"), element(null, ":hover")));
    r1.add(rule(prop("color"), color("#000")));
    r1.add(rule(prop("margin"), dim(12, Unit.PX)));
    
    r0.add(r1);
    root.add(r0);
    
    Options opts = new Options();
//    CssRenderer cr = new CssRenderer(new Context(opts));
//    System.out.println(cr.render(root));
  }
  
  @Test
  public void testBasic2() throws LessException {
//    LessStylesheet less = new LessStylesheet();
    
    Selector s0 = selector(element(DESC, "h1"), element(CHILD, "b"), element(SIB_ADJ, "p"));
    Selector s1 = selector(element(CHILD, "p"), element(DESC, "span"));
    
    Ruleset r0 = ruleset(s0, s1);
    
    Quoted q0 = quoted(APOSTROPHE, "bar", var("@baz"));
    Quoted q1 = quoted(QUOTATION_MARK, "bar");

    r0.add(def("@foo", value(q0, q1)));
    r0.add(def("color", color("#000")));
    r0.add(rule(prop("foo-bar"), var("@foo")));

//    LessEnv env = new LessEnv();
//    r0.toCSS(env);
//    System.out.println(env.buffer().toString());
    
    System.out.println("-----------------------");
    
    Ruleset rs = ruleset(s0);
    s1 = selector(element("body"));
    rs.add(rule(prop("foo-bar"), value(q0, q1), false));

    Quoted q2 = quoted(QUOTATION_MARK, "xxx");
    rs.add(new Rule(new Variable("@foo"), q2, false));
    RGBColor color = RGBColor.fromHex("#123");
    rs.add(new Rule(new Property("background"), color, false));


    Ruleset inner = ruleset(s1);
    inner.add(rule(prop("background"), var("@foo"), false));
    rs.add(inner);

//    env = new LessEnv();
//    rs.toCSS(env);
//    System.out.println(env.buffer().toString());
  }
  
  @Test
  public void testBasic3() throws LessException {
    Options opts = new Options(4);
    
    Ruleset r1 = ruleset(selector(element(DESC, "span")));
    r1.add(rule(prop("font-size"), dim(12, Unit.PX), false));
    r1.add(rule(prop("color"), color("#111"), false));

    Ruleset r2 = ruleset(selector(element(CHILD, ".class")));
    r2.add(rule(prop("color"), color("#000"), false));
    
    Media m1 = new Media();
//    m1.addCondition("bar");
    m1.add(r2);
    
    Media m0 = new Media();
//    m0.addCondition("foo");
    m0.add(r1);
    m0.add(m1);
    
    Ruleset r0 = ruleset(selector(element(DESC, "h2")));
    r0.add(rule(prop("color"), color("#f00"), false));
    r0.add(rule(prop("background"), color("#222"), false));
    r0.add(m0);
    
    Stylesheet s = sheet();
    s.add(r0);

//    CssRenderer w = new CssRenderer(new Context(opts));
//    System.out.println(w.render(s));
  }

  
  @Test
  public void testCartesianProduct() throws LessException {
    Ruleset r0 = ruleset(selector(element("a")), selector(element("b")));
    r0.add(rule(prop("color"), color("#000"), false));
    Ruleset r1 = ruleset(selector(element("&"), element(SIB_ADJ, "&")));
    r1.add(rule(prop("background"), color("#fff"), false));
    r0.add(r1);

    Stylesheet root = sheet();
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testDescendant() throws LessException {
    Stylesheet root = sheet();
    Ruleset r0 = ruleset(selector(element(".parent")));
    Ruleset r1 = ruleset(selector(element(DESC, ".child")));
    r1.add(rule(prop("color"), color("#000"), false));
    r0.add(r1);
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testMedia() throws LessException {
    Ruleset r0 = ruleset(selector(element(".one")));
    r0.add(rule(prop("background"), color("#fff"), true));
    r0.add(rule(prop("font-size"), dim(12, Unit.PX)));
    
    Media m0 = new Media();
//    m0.addCondition("foo");
    
    Ruleset r1 = ruleset(selector(element(".two")));
    r1.add(rule(prop("color"), color("#000")));
    r1.add(rule(prop("font-size"), dim(10, Unit.EM)));
    r1.add(rule(prop("background"), color("#fff"), true));

    m0.add(r1);
    r0.add(m0);
    
    Stylesheet root = sheet();
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testMixin() throws LessException {
    Stylesheet root = sheet();
    Ruleset r1 = ruleset(selector(element(".foo")));
    root.add(r1);
    
    Mixin m0 = new Mixin("xyz", new MixinParams(), null);
    m0.add(rule(prop("background"), color("#fff"), false));
    MixinCall c0 = new MixinCall(selector(element("xyz")), null, false);

    r1.add(rule(prop("color"), color("#000"), false));
    r1.add(m0);
    r1.add(c0);

//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }
  
  @Test
  public void testMixinClosure() throws LessException {
    Stylesheet root = sheet();
    root.add(def("@color", color("#010203")));
    
    Ruleset r0 = ruleset(selector(element(".one")));
    r0.add(new MixinCall(selector(element(".mixin-1")), null, false));
    r0.add(new MixinCall(selector(element(".mixin-2")), null, false));
    root.add(r0);
    
    Mixin m0 = new Mixin(".mixin-1", new MixinParams(), null);
    m0.add(rule(prop("color"), var("@color"), false));
    root.add(m0);

    Ruleset m1 = ruleset(selector(element(".mixin-2")));
    m1.add(rule(prop("color"), var("@color"), false));
    root.add(m1);
    
    Ruleset r1 = ruleset(selector(element(".two")));
    r1.add(new MixinCall(selector(element(".mixin-1")), null, false));
    r1.add(new MixinCall(selector(element(".mixin-2")), null, false));
    r1.add(def("@color", color("#000")));
    
//    Ruleset r2 = ruleset(selector(element(".ignore-me")));
//    r2.add(rule(prop("foo"), quoted('"', "foo"), false));
//    root.add(r2);
    
    root.add(r1);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testRulesetEmpty() throws LessException {
    Ruleset r0 = ruleset(selector(element("h1"), element(null, ".class")));
    Ruleset r1 = ruleset(selector(element("p"), element("span")));
    r0.add(r1);
    Stylesheet root = sheet();
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    Assert.assertEquals(cr.render(root), "");
  }
  
  @Test
  public void testRulesets() throws LessException {
    Selector s0 = selector(element("h1"), element(null, ".foo"));
    Selector s1 = selector(element("h2"), element(null, ".bar"));
    Ruleset r0 = ruleset(s0, s1);
    r0.add(rule(prop("color"), color("#000"), false));

    Selector s2 = selector(element("&"), element(null, ":hover"));
    Ruleset r1 = ruleset(s2);
    r1.add(rule(prop("background"), color("#fff"), false));

    r0.add(r1);
    
    Selector s3 = selector(element(DESC, "*"), element(CHILD, "p"));
    Selector s4 = selector(element(DESC, "b"));
    Ruleset r2 = ruleset(s3, s4);
    r2.add(rule(prop("margin"), dim(12, Unit.PX), true));
    r2.add(rule(prop("background"), var("@foo"), false));
    
    r1.add(r2);

    Stylesheet root = new Stylesheet();
    root.add(def("@foo", color("#333")));
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testVarElement() throws LessException {
    Stylesheet root = sheet();
    root.add(def("@foo", quoted(Chars.NULL, "span")));
    
    Selector s0 = selector(element(".bar"));
    Ruleset r1 = ruleset(s0);
    
    Selector s1 = selector(element(DESC, var("@foo")));
    Ruleset r2 = ruleset(s1);
    r2.add(rule(prop("color"), color("#fff"), false));
    
    r1.add(r2);
    root.add(r1);

//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testWildcard3() throws LessException {
    Ruleset r0 = ruleset(selector(element(".child")), selector(element(".sibling")));

    Ruleset r1 = ruleset(selector(element(".parent"), element(DESC, "&")));
    r1.add(rule(prop("color"), color("#000"), false));
    r0.add(r1);

    Ruleset r2 = ruleset(selector(element("&"), element(SIB_ADJ, "&")));
    r2.add(rule(prop("color"), color("red"), false));
    r0.add(r2);

    Stylesheet root = sheet();
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }


  
  @Test
  public void testWildcard2() throws LessException {
    Ruleset r0 = ruleset(selector(element(".child")), selector(element(".sibling")));

    Ruleset r2 = ruleset(selector(element("&"), element(SIB_ADJ, "&")));
    r2.add(rule(prop("color"), color("red"), false));
    r0.add(r2);

    Stylesheet root = sheet();
    root.add(r0);
    
//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testWildcard() throws LessException {
    Selector s0 = selector(element(".parent"));
    Ruleset r0 = ruleset(s0);
    
    Selector s1 = selector(element(Combinator.CHILD, ""), element(null, "&"), element(null, ":hover"));
    Ruleset r1 = ruleset(s1);
    r1.add(rule(prop("color"), color("#fff"), false));
    r1.add(rule(prop("font-size"), dim(12, Unit.PX)));
    r0.add(r1);

    Stylesheet root = sheet();
    root.add(r0);

//    CssRenderer cr = new CssRenderer(new Context());
//    System.out.println(cr.render(root));
  }

  @Test
  public void testWildcard4() throws LessException {
    Ruleset r1 = ruleset(selector(element("parent"), element(null, ".class")), selector(element("pop")));
    r1.add(rule(prop("color"), color("#fff")));
    
    Ruleset r2 = ruleset(selector(element("&"), element(DESC, "span")));
    r2.add(rule(prop("font-size"), dim(12, Unit.PX), true));
    r2.add(rule(prop("color"), color("#a03")));
    r1.add(r2);
    
    Stylesheet s = sheet();
    s.add(r1);
    
//    CssRenderer r = new CssRenderer(new Context(new Options(4)));
//    System.out.println(r.render(s));
  }

}
