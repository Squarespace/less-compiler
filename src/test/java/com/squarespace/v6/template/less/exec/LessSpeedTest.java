package com.squarespace.v6.template.less.exec;

import java.util.Arrays;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Stylesheet;


public class LessSpeedTest extends LessTestBase {

  List<String> SCRIPTS = Arrays.asList(
      ".m(@arg) { @a: @arg + 1; .foo { a: @arg + 1; } } .rule { @a: 3; .m(10); @a: 4; a: @a }",
      "@a: 1; .rule { prop: @a; .foo, .bar { color: #123; .xyz { background: red; } } }",
      "@a: 'foo'; @b: @a 'bar'; .rule{prop: @b;}",
      ".m(@a: 1) { @b: @a + 1 } .m(@c) { @d: @c; } .rule { .m(2); prop: @b; prop: @d; }",
      "@foo: 'bob'; @a: ~'foo'; @b: @@a; .rule {; other: @@a @b; }",
      ".m(@arg) when (@arg > 0) { @a: @arg; .sub { prop: @arg; } .m(@arg - 1); } .rule { .m(4); a: @a }",
      ".o() { e: 4; } .m() { a: 1; .sub { b: 2; .sub2 { d: 4; } } .o; } .rule { .m !important; c: 3; }",
      ".rule { @a: 1; .m() { a: @a; @b: 2; } } .parent { .rule.m; b: @b; }",
      ".sibling, .child { & + & { a: 1 } }",
      ".rule { .m { a: 1 } } .parent { .rule.m; }",
      "/* foo */ .bar { a: 1 } @media screen { foo: 1; .rule { b: 1 } }",
      "@page { @page { .rule { a: 1; } } }",
      ".m1() { @a: 2; } .m2(@arg) { @b: @a + @arg; .rule { prop: @b; } } .rule { @a: 1; .m1; .m2(3); f: @a; b: @b; }",
      ".rule { text-shadow: 1px 0 lighten(#14a0ff, -5%); }",
      ".rule { color: _color('evil red'); }",
      ".rule { red: red(#f00); }",
      ".rule { contrast-white: contrast(#fff); contrast-black: contrast(#000); contrast-red: contrast(#ff0000); }",
      ".r { contrast-light: contrast(#fff, #111111, #eeeeee); contrast-dark: contrast(#000, #111111, #eeeeee); }",
      ".r { mix: mix(#ff0000, #ffff00, 80); mix-0: mix(#ff0000, #ffff00, 0); }",
      ".rule { shade: shade(#777777, 13); }",
      ".rule { mix-weightless: mix(#ff0000, #ffff00); }",
      ".rule { softlight: softlight(#f60000, #ffffff); }",
      ".mixin(@index, @b: 2, @c: 3, @rest ...) when (@index >= 0) { "
          + ".foo() { value: 1; } .foo; .mixin(@index - 1, @b, @c); } .rule { .mixin(50, 1, 2, 3, 4, 5); }",
      "variable-names  { @var: 'hello'; @name: 'var'; name: @@name;}",
      "@a: 2; a:nth-child(@a) { border: 1px; }",
      "label:not(.shipping-option-label) { }",
      "label { color: color: #111; }",
      ".foo { margin: ; }"
      );

  
//  @Test  // enable for manual performance testing.
  public void testSpeed() throws LessException {
    int size = SCRIPTS.size();
    for (int i = 1; i < 8; i *= 2) {
      for (int j = 1; j <= size; j++) {
        System.out.println(j + " scripts");
        run(SCRIPTS.subList(0, j), 1000 * i);
      }
      System.out.println();
    }
  }
  
  private void run(List<String> scripts, int iters) throws LessException {
    long start = System.nanoTime();
    for (int i = 0; i < iters; i++) {
      for (String script : scripts) {
        LessHarness h = new LessHarness();
        Node node = h.parse(script);
        LessEvaluator machine = new LessEvaluator(h.context());
        machine.render((Stylesheet)node);
      }
    }
    double elapsed = (System.nanoTime() - start) / 1000000.0;
    double per = elapsed / iters;
    System.out.println(iters + " iterations - elapsed " + elapsed + " (" + per + ") ms/iter");
  }

//  @Test  // enable for manual performance testing and profiling
  public void testVerbose() throws LessException {
    for (String script : SCRIPTS) {
      try {
        runVerbose(script);
      } catch (LessException e) {
        e.printStackTrace();
      }
    }
  }
  
  private void runVerbose(String script) throws LessException {
    System.out.println("Executing:\n" + script + "\n-------------------------------------");
    LessHarness h = new LessHarness();
    Node node = h.parse(script);
  
    LessEvaluator machine = new LessEvaluator(h.context());
    String result = machine.render((Stylesheet)node);
    System.out.println(result);
    System.out.println("===========================================");
  }
  
}
