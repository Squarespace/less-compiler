package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Operator.ADD;
import static com.squarespace.v6.template.less.model.Operator.MULTIPLY;
import static com.squarespace.v6.template.less.model.Operator.SUBTRACT;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Expression;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Operation;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.parse.LessStream;
import com.squarespace.v6.template.less.parse.Parselets;


public class ParseletTest extends LessTestBase {

  @Test
  public void testAddition() throws LessException {
    String[] strings = new String[] {
      "3 - 4 * 7 + @foo",
      "4 / 7.2 * 2 - 1",
      "/* x */ \n  1 -  /* y */  2px * 4in"
    };
    for (String str : strings) {
      System.out.println(str);
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.ADDITION));
    }
  }
  
//  @Test
//  public void testAlpha() throws LessException {
//    String[] strings = new String[] {
//      "alpha(opacity=1)",
//      "alpha(opacity=@foo)",
//      "alpha(opacity=)"
//    };
//    for (String str : strings) {
//      LessStream p = new LessStream(str);
//      System.out.println(p.parse(Parselets.FUNCTION_CALL));
//    }
//  }
  
  
//  @Test
//  public void testBlockDirective() throws LessException {
//    String[] strings = new String[] {
//        "@page foo { @namespace foo bar; color: #000; font-size: 12px !important }",
//        "@page foo { .foo { @media bar and baz { @page bar { color: blue } } } }"
//    };
//    for (String str : strings) {
//      LessStream stm = new LessStream(str);
//      Stylesheet sheet = (Stylesheet) stm.parse(Parselets.STYLESHEET);
////      CssRenderer cr = new CssRenderer(new Context(new Options(4)));
////      System.out.println(cr.render(sheet));
//    }
//
//  }
  
//  @Test
//  public void testColor() throws LessException {
//    String[] strings = new String[] {
//        "#fff",
//        "#123456",
//        "#aaaa",
//        "#xyz"
//    };
//    for (String s : strings) {
//      LessStream p = new LessStream(s);
//      System.out.println(p.parse(Parselets.COLOR));
//    }
//  }
  
//  @Test
//  public void testComment() throws LessException {
//    String[] strings = new String[] {
//      "/* foo */",
//      "// bar baz"
//    };
//    for (String s : strings) {
//      LessStream p = new LessStream(s);
//      System.out.println(p.parse(Parselets.COMMENT));
//    }
//  }
  
  
  @Test
  public void testDirective() throws LessException {
    String[] strings = new String[] {
        ".foo { @namespace foo bar; color: #000; font-size: 12px !important }"
    };
    for (String str : strings) {
      LessStream stm = new LessStream(str);
      Stylesheet sheet = (Stylesheet) stm.parse(Parselets.STYLESHEET);
//      CssRenderer cr = new CssRenderer(new Context(new Options(4)));
//      System.out.println(cr.render(sheet));
    }
  }
  
  @Test
  public void testElement() throws LessException {
    String[] strings = new String[] { 
      " h1",
      ">foo",
      "+:hover",
      "~.class-one",
      ">&",
      "foo"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.ELEMENT));
    }
  }
  
  @Test
  public void testEntity() throws LessException {
    String[] strings = new String[] {
      "\"bar @{baz} 123\"",
      "\"foo @@{xyz} bar\"",
      "\"@@@{foo}bar\"",
      "/* foo bar // */"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.ENTITY));
    }
  }
  
  @Test
  public void testExpression() throws LessException {
    String[] strings = new String[] {
      "+12px -3px",
      "'bar' 3 + 1",
      "1px solid black",
      "1 * (2 - (3 / 2))",
      "-@foo"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.EXPRESSION));
    }
  }
  
  @Test
  public void testExpression2() throws LessException {
    String ex = "1 - 3 * 4 + 2";
    LessStream p = new LessStream(ex);
    System.out.println(p.parse(Parselets.EXPRESSION));
    
    Operation op2 = oper(MULTIPLY, oper(SUBTRACT, dim(1), dim(3)), oper(ADD, dim(4), dim(2)));
    Expression expn = new Expression(Arrays.<Node>asList(op2));
    System.out.println(expn);
  }
  
  @Test
  public void testExpressionList() throws LessException {
    String[] strings = new String[] {
        "1 + 3, 4 * (7 + 3)",
        "-1 * 4 - 3"
      };
      for (String str : strings) {
        LessStream p = new LessStream(str);
        System.out.println(p.parse(Parselets.EXPRESSION_LIST));
      }
  }
  
  @Test
  public void testFunctionCall() throws LessException {
    String[] strings = new String[] {
      "progid:DXImageTransform.Microsoft.Alpha( opacity=50 )",
      "foo(1, 2, 3)",
      "rgba(32, 32, 32, .2)",
      "rgba( 10, \t 20, \t 30, \t 0.5 )",
      "test(\"foo\")"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.FUNCTION_CALL));
    }
  }
  
  @Test
  public void testLiteral() throws LessException {
    String[] strings = new String[] {
      "'foo'",
      "12/13",
      "3.14px"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.LITERAL));
    }
  }
  
  @Test
  public void testMedia() throws LessException {
    String[] strings = new String[] {
      "@media screen and (width: @var) { .bar { color: #fff }}",
      "@media screen and ((foo: bar)) { color: #fff; }"
    };
    for (String str : strings) {
      LessStream stm = new LessStream(str);
      System.out.println(stm.parse(Parselets.DIRECTIVE));
    }
  }
  
  @Test
  public void testMixinCall() throws LessException {
    String[] strings = new String[] {
      ".x",
      ".a > .b > .c",
      ".foo > .bar;"
    };
    for (String str : strings) {
      LessStream stm = new LessStream(str);
      System.out.println(stm.parse(Parselets.MIXIN_CALL));
    }
  }

  @Test
  public void testMixinCallArgs() throws LessException {
    String[] strings = new String[] {
      "(  )",
      "('@{x}y@{z}')",
      "('foo', 1, 2, 3/16)",
      "(@a: 1, 2, 3; @b: 2)",
      "(1,2,3; @b;)",
      "(@a, @b, @c: 'foo')",
      "(@a @b, @c)"
    };
    for (String str : strings) {
      System.out.println(str);
      LessStream stm = new LessStream(str);
      System.out.println(stm.parse(Parselets.MIXIN_CALL_ARGS));
    }
  }
  
  @Test
  public void testMixinDefinition() throws LessException {
    String[] strings = new String[] {
      ".foo(@a, @b: 12px, ...) { }",
      "#bar(@a: foo, @b ...) { }",
      ".foo(@a, @b) { color: white }"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.MIXIN));
    }
  }
  
  @Test
  public void testMultiply() throws LessException {
    String[] strings = new String[] {
      "3 * 4 * 5"//,
//      "1.2 * 4"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      Node node = p.parse(Parselets.ADDITION);
      System.out.println(node);
      System.out.println(node.eval(null));
      Assert.assertEquals(node.eval(null), dim(60, null));
    }
  }
  
  @Test
  public void testNegate() throws LessException {
    String[] strings = new String[] {
      "1 * -@foo"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.ADDITION));
    }
  }
  
  @Test
  public void testOperand() throws LessException {
    String[] strings = new String[] {
      "3"  
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.OPERAND));
    }
  }
  
  @Test
  public void testProperty() throws LessException {
    String[] strings = new String[] {
        "-moz-foo-bar :",
        "*-foo:"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.PROPERTY));
    }
  }
  
  @Test
  public void testQuoted() throws LessException {
    String[] strings = new String[] {
        " \"foo ' \\\" bar \"",
        "\"12345",
        "'foo\"bar'",
        "'foo @{bar} baz @{'",
        "'foo @{ bar } baz'",
        "~'foo bar'"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.QUOTED));
    }
  }

  @Test
  public void testRatio() throws LessException {
    String[] strings = new String[] {
        "15/30",
        "7/9",
        "foo/bar"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.RATIO));
    }
  }
  
  @Test
  public void testRule() throws LessException {
    String[] strings = new String[] {
//      "color: #fff;",
//      "font:italic bold 12px/30px Georgia, serif;",
//      "background: @foo @bar;",
//      "@foo: 12px;",
//      "c: green;",
//      "color: #f00 + white;",
//      "color: black + white;",
//      "color: red + green",
//      "radius: 2px /* foo */;",
      "color: 1px //put in @b - causes problems! --->;"
    };
    for (String str : strings) {
      LessHarness h = new LessHarness(Parselets.RULE);
      System.out.println(h.parse(str));
//      LessStream p = new LessStream(str);
//      System.out.println(p.parse(LessStream.RULE));
    }
  }
  
  @Test
  public void testPrimary() throws LessException {
    String[] strings = new String[] {
      "/* lost comment */  content: \"content\";"  
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.PRIMARY));
    }
  }
  
  @Test
  public void testRuleset() throws LessException {
    String[] strings = new String[] {
      ".foo:hover, .bar.class { color: #aaa; font-size: 12px }",
      "span[class~=\"foo\"], p[foo], b[\"bar\"] { color: #000; }"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.RULESET));
    }
  }
  
  @Test
  public void testRuleset2() throws LessException {
    String[] strings = new String[] {
      "#ns { .foo { color: #f00; } &.class { color: #00f } }",
      ".parent, .sibling { & + & { color: #fff; } }",
      ".a { &.b, &.c { d & { color: #fff } color: black }}"
    };
    for (String str : strings) {
      LessStream stm = new LessStream(str);
      Stylesheet sheet = (Stylesheet) stm.parse(Parselets.STYLESHEET);
//      CssRenderer cr = new CssRenderer(new Context(new Options(4)));
//      System.out.println(cr.render(sheet));
    }

  }
  
  @Test
  public void testSelector() throws LessException {
    String[] strings = new String[] { 
      ".foo > bar.baz",
      " h1:hover + p.class-two",
      "span[class~=\"foo @{var}\"]"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.SELECTOR));
    }
  }
  
  @Test
  public void testShorthand() throws LessException {
    String[] strings = new String[] {
      "small/12px",
      "small/@value",
      "foo-bar/124",
      "@bar/@baz",
      "blue/12px"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.SHORTHAND));
    }
  }
  
  @Test
  public void testStylesheet() throws LessException {
    String[] strings = new String[] {
      ".a { .b, .c { .d { color: #fff }}}",
      ".foo { color: white; .bar { font-size: 12px } background: 12; }",
      ".x { .y { .a, .b, & { color: blue; } } }",
      ".foo span{color:black;font-size:12px}",
      "@foo: 1.4em;.x { font-size: @foo;}",
      ".foo{font:small/12px}",
      ".parent, .sibling { & + & { color: #fff; }}",
      ".x { &:hover, > &.class { color: #fff; } }"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      Stylesheet sheet = (Stylesheet) p.parse(Parselets.STYLESHEET);
      System.out.println("----------");
//      CssRenderer cr = new CssRenderer(new Context(new Options(4)));
//      System.out.println(cr.render(sheet));
    }
  }
  
  @Test
  public void testUnicodeRange() throws LessException {
    String[] strings = new String[] {
        "U+A?-B",
        "U+x"
    };
    for (String str : strings) {
      LessStream p = new LessStream(str);
      System.out.println(p.parse(Parselets.UNICODE_RANGE));
    }
  }

// MOVED
//  @Test
//  public void testUrl() throws LessException {
//    String[] strings = new String[] {
//      "url(\"http://foo.com/@{bar}\")",
//      "url(http://glonk.com)"
//    };
//    for (String str : strings) {
//      LessStream p = new LessStream(str);
//      System.out.println(p.parse(LessStream.FUNCTION_CALL));
//    }
//  }

// MOVED
//  @Test
//  public void testVariable() throws LessException {
//    String[] strings = new String[] {
//      "@foo",
//      "@bar-baz",
//      "@123",
//      "@@indirect",
//    };
//    for (String str : strings) {
//      LessStream p = new LessStream(str);
//      System.out.println(p.parse(LessStream.VARIABLE));
//    }
//  }

// MOVED
//  @Test
//  public void testVariableRef() throws LessException {
//    String[] strings = new String[] {
//      "@{foo}",
//      "@{bar-baz}",
//      "@{ xyz }"
//    };
//    for (String str : strings) {
//      LessStream p = new LessStream(str);
//      System.out.println(p.parse(LessStream.VARIABLE_CURLY));
//    }
//  }
}
