package com.squarespace.less.parse2;

import static com.squarespace.less.SyntaxErrorType.ALPHA_UNITS_INVALID;
import static com.squarespace.less.SyntaxErrorType.EXPECTED;
import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.less.model.Operator.ADD;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessErrorType;
import com.squarespace.less.LessException;
import com.squarespace.less.LessMessages;
import com.squarespace.less.LessOptions;
import com.squarespace.less.SyntaxErrorType;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessMaker;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operator;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.LessStream;
import com.squarespace.less.parse.Parselets;

public class LessParserTest extends LessMaker {

  @Test
  public void testDeepNestingRulesets() throws LessException {
    Tester t = tester(LessSyntax.RULESET);

    t.ok("a { b { c { d { e { f { g { h { i { j { k { l { m { n { o { p { q { r { s { color: red; } } } } } } } } } } } } } } } } } } }",
        rs("a", rs("b", rs("c", rs("d", rs("e",
            rs("f", rs("g", rs("h", rs("i", rs("j",
                rs("k", rs("l", rs("m", rs("n", rs("o", rs("p", rs("q", rs("r", rs("s",
                    rule(prop("color"), color("red"))))))))))))))))))))));
  }

  @Test
  public void testDeepNestingMath() throws LessException {
    Tester t = tester(LessSyntax.ADDITION);

    t.ok("1+(2+(3+(4+(5+(6+(7+(8+(9+(10+(11+(12+(13+(14+(15+(16+(17+(18)))))))))))))))))",
        oper(ADD, dim(1),
            oper(ADD, dim(2),
                oper(ADD, dim(3),
                    oper(ADD, dim(4),
                        oper(ADD, dim(5),
                            oper(ADD, dim(6),
                                oper(ADD, dim(7),
                                    oper(ADD, dim(8),
                                        oper(ADD, dim(9),
                                            oper(ADD, dim(10),
                                                oper(ADD, dim(11),
                                                    oper(ADD, dim(12),
                                                        oper(ADD, dim(13),
                                                            oper(ADD, dim(14),
                                                                oper(ADD, dim(15),
                                                                    oper(ADD, dim(16),
                                                                        oper(ADD, dim(17), dim(18)))))))))))))))))));
  }

  public Ruleset rs(String name, Node child) {
    Ruleset r = new Ruleset();
    r.add(selector(element(name)));
    r.add(child);
    return r;
  }

  @Test
  public void testAddition() throws LessException {
    Tester t = tester(LessSyntax.ADDITION);
    t.ok("1 + 2", oper(Operator.ADD, dim(1), dim(2)));
    t.ok("1 - 2", oper(Operator.SUBTRACT, dim(1), dim(2)));
    t.ok("3 - ( /* foo */ 2 * 7 )",
        oper(Operator.SUBTRACT, dim(3),
        expn(comment(" foo ", true), oper(Operator.MULTIPLY, dim(2), dim(7)))));

    t.fail("1 -2", INCOMPLETE_PARSE);
    t.fail("1 ! ", INCOMPLETE_PARSE);
    t.fail("1 + ", INCOMPLETE_PARSE);
    t.fail("x + y ", INCOMPLETE_PARSE);
    t.fail("1 + y ", INCOMPLETE_PARSE);
    t.fail("1 + ! ", INCOMPLETE_PARSE);
  }

  @Test
  public void testAlpha() throws LessException {
    Tester t = tester(LessSyntax.ALPHA);

    t.ok("opacity=2)", alpha(dim(2)));
    t.ok("opacity=@foo)", alpha(var("@foo")));
    t.ok("opacity=)", alpha(anon("")));
    t.ok("OpAciTY=0.5 \n )", alpha(dim(0.5)));

    t.fail("opacity=2px)", ALPHA_UNITS_INVALID);
    t.fail("opacity=2!", SyntaxErrorType.EXPECTED);
    t.fail("opacity=!", SyntaxErrorType.EXPECTED);
    t.fail("x", INCOMPLETE_PARSE);
  }

  @Test
  public void testAssignment() throws LessException {
    Tester t = tester(LessSyntax.ASSIGNMENT);

    t.ok("foo=123", assign("foo", dim(123)));
    t.ok("foo = 123", assign("foo", dim(123)));

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("foo!", INCOMPLETE_PARSE);
    t.fail("foo", INCOMPLETE_PARSE);
    t.fail("foo =", INCOMPLETE_PARSE);
    t.fail("foo == 123", INCOMPLETE_PARSE);
    t.fail("foo !!", INCOMPLETE_PARSE);
    t.fail("foo = !", INCOMPLETE_PARSE);
  }

  @Test
  public void testColor() throws LessException {
    Tester t = new Tester(LessSyntax.COLOR);

    t.ok("#abc123", color("abc123"));
    t.ok("#123", color("123"));
    t.ok("#000", color("000"));

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("#11", INCOMPLETE_PARSE);
    t.fail("#11z", INCOMPLETE_PARSE);
    t.fail("#0000", INCOMPLETE_PARSE);
  }

  @Test
  public void testColorKeyword() throws LessException {
    Tester t = new Tester(LessSyntax.COLOR_KEYWORD);

    t.ok("green", color("green"));

    t.fail("+", INCOMPLETE_PARSE);
    t.fail("gree", INCOMPLETE_PARSE);
  }

  @Test
  public void testComments() throws LessException {
    Tester t = new Tester(LessSyntax.COMMENT);

    t.ok("// foo", comment(" foo", false));
    t.ok("//", comment("", false));
    t.ok("//\n", comment("", false));
    t.ok("//\n\n", comment("", false));
    t.ok("/* foo */", comment(" foo ", true));
    t.ok("/**/", comment("", true));
    t.ok("/*\n foo\n */", comment("\n foo\n ", true));
    t.ok("/*! retain me */", comment("! retain me ", true));

    // hit eof, counts
    t.ok("/*\n foo\n *", comment("\n foo\n *", true));

    // eof is just null, since it's not incomplete
    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("/", INCOMPLETE_PARSE);
    t.fail("/!", INCOMPLETE_PARSE);
    t.fail("*", INCOMPLETE_PARSE);
    t.fail("123", INCOMPLETE_PARSE);

    // When comments are ignored, a valid comment will return the dummy
    // comment "<ignore me>" which will never be included in the syntax tree.
    t.ignoreComments();
    t.ok("/* foo */", comment("<ignore me>", false));
    t.ok("/*! retain */", comment("! retain ", true));
  }

  @Test
  public void testCondition() throws LessException {
    Tester t = new Tester(LessSyntax.CONDITION);

    t.ok("(@a = 1)", cond(Operator.EQUAL, var("@a"), dim(1)));
    t.ok("(@a <= 12px)", cond(Operator.LESS_THAN_OR_EQUAL, var("@a"), dim(12, Unit.PX)));
    t.ok(" not ('foo' = 'bar')", cond(Operator.EQUAL, quoted('\'', false, "foo"), quoted('\'', false, "bar"), true));
    t.ok("not(@a!=1)", cond(Operator.NOT_EQUAL, var("@a"), dim(1), true));
    t.ok("(baz = @name)", cond(Operator.EQUAL, kwd("baz"), var("@name")));

    t.fail("not @a = 1", EXPECTED);
    t.fail("(@a ^ 1)", EXPECTED);
    t.fail("(. = .)", EXPECTED);
    t.fail("(@a =", EXPECTED);
  }

  @Test
  public void testConditions() throws LessException {
    Tester t = new Tester(LessSyntax.CONDITIONS);

    t.ok("(@a = 1) and (@b >= 2)",
        cond(Operator.AND,
            cond(Operator.EQUAL, var("@a"), dim(1)),
            cond(Operator.GREATER_THAN_OR_EQUAL, var("@b"), dim(2))));

    t.ok("(@a = 1)", cond(Operator.EQUAL, var("@a"), dim(1)));

    t.fail(" not foo = 1 and (bar = 2)", EXPECTED);
  }

  @Test
  public void testDefinition() throws LessException {
    Tester t = new Tester(LessSyntax.DEFINITION);

    t.ok("@foo: red", def("@foo", color("red")));
    t.ok("@foo: ;", def("@foo", anon("")));
    t.ok("@foo : 123 ;", def("@foo", dim(123)));
    t.ok("@foo: 123;", def("@foo", dim(123)));
    t.ok("@123: 123", def("@123", dim(123)));
    t.ok("@123: 123 !important", def("@123", dim(123)));

    // TODO: there are some coverage gaps where the parsing logic may be simplified

    t.fail("@foo: !!;", INCOMPLETE_PARSE);
    t.fail("@123: 1  !foo", INCOMPLETE_PARSE);
    t.fail("!", INCOMPLETE_PARSE);
    t.fail("$foo", INCOMPLETE_PARSE);
    t.fail("@foo = 123", INCOMPLETE_PARSE);
    t.fail("@foo: }", INCOMPLETE_PARSE);
    t.fail("@foo: 1 }", INCOMPLETE_PARSE);
    t.fail("@!x", INCOMPLETE_PARSE);
  }

  @Test
  public void testDimension() throws LessException {
    Tester t = new Tester(LessSyntax.DIMENSION);

    t.ok("-12.57px", dim(-12.57, Unit.PX));
    t.ok(".5", dim(0.5));
    t.ok("+3.14159", dim(3.14159));

    t.fail("ab", INCOMPLETE_PARSE);
    t.fail("15xyz", INCOMPLETE_PARSE); // unknown unit
  }

  @Test
  public void testDirective() throws LessException {
    Tester t = new Tester(LessSyntax.DIRECTIVE);

    t.ok("@media { }", media(null));

    t.ok("@media a and b { }",
        media(features(expn(kwd("a"), kwd("and"), kwd("b")))));

    t.ok("@media c and d, e { }",
        media(features(
            expn(kwd("c"), kwd("and"), kwd("d")),
            expn(kwd("e")))));

    t.ok("@media (min-width: 48em) { }",
        media(features(expn(paren(
            feature(prop("min-width"), dim(48, Unit.EM)))))));

    t.ok("@media", media(features()));

    // TODO: test with safe mode disabled. unclosed media directives are a bug
  }

  /**
   * Test harness for parser test cases.
   */
  private static class Tester {

    LessSyntax syntax;
    LessMessages messages = new LessMessages(4, 10);
    boolean ignoreComments = false;

    public Tester(LessSyntax syntax) {
      this.syntax = syntax;
    }

    public void ignoreComments() {
      this.ignoreComments = true;
    }

    public void ok(String source, Node expected) throws LessException {
      LessOptions opts = new LessOptions();
      opts.ignoreComments(ignoreComments);

      LessContext ctx = new LessContext(opts);
      LessParser parser = new LessParser(ctx, source);

      Node actual = null;
      try {
        actual = parser.parse(syntax);

      } catch (LessException e) {
        String error = messages.formatError(e);
        Assert.fail(error);
      }
      if (!equals(actual, expected)) {
        String msg = "EXPECTED:\n\n" + repr(ctx, expected) + "\n\nGOT:\n\n" + repr(ctx, actual) + "\n";
        Assert.fail(msg);
      }
    }

    public void fail(String source, SyntaxErrorType expected) {
      LessContext ctx = new LessContext();
      LessParser parser = new LessParser(ctx, source);
      try {
        Node node = parser.parse(syntax);
        Assert.fail("expected case to fail, but got:\n\n" + repr(ctx, node) + "\n");
      } catch (LessException e) {
        LessErrorType actual = e.primaryError().type();
        if (actual != expected) {
          Assert.fail("Expected " + expected + " got " + actual + "\n" + messages.formatError(e));
        }
        // Expected
      }
    }

    private boolean equals(Node actual, Node expected) {
      if (expected == null) {
        return actual == null;
      }
      return expected.equals(actual);
    }

    private String repr(LessContext ctx, Node node) {
      String repr = "<null result>";
      if (node != null) {
        Buffer buf = ctx.newBuffer();
        buf.incrIndent().indent();
        node.modelRepr(buf);
        repr = buf.toString();
      }
      return repr;
    }
  }

  private static Tester tester() {
    return new Tester(LessSyntax.STYLESHEET);
  }

  private static Tester tester(LessSyntax syntax) {
    return new Tester(syntax);
  }

  // =============================================================

//  @Test
  public void testFile() throws Exception {
    String fileName;
    String source;

    fileName = "def.less";
    fileName = "expression.less";
    fileName = "col2.less";
    fileName = "src/test/resources/test-suite/less/comment-eof.less";
//    fileName = "bug6.less";
//    fileName = "bug7.less";
//    fileName = "bug8.less";
//    fileName = "bug12.less";
//    fileName = "e5.less";
//    fileName = "tmp.less";
    source = LessUtils.readFile(Paths.get(fileName));

//    source = "h1 /* a */ span:hover /* b */ {\n"
//        + "    color: /* c */ red;\n"
//        + "}";

//    source = ".mixin-guard {\n"
//        + "    .mixin-0( @a  :  12px  ) {\n"
//        + "        dummy: rule;\n"
//        + "    }\n"
//        + "    .mixin-0(   );\n"
//        + "}";

//    source = ".ruleset-1 {\n"
//        + "    #ns.m1 .m2;\n"
//        + "}\n"
//        + "\n"
//        + ".ruleset-2 {\n"
//        + "    #ns > .m1.m2;\n"
//        + "}";

    System.out.println(source);
    LessContext ctx = new LessContext();
    LessParser parser = new LessParser(ctx, source);
    Stylesheet stylesheet = (Stylesheet) parser.parse(LessSyntax.STYLESHEET);

    Buffer buf = ctx.newBuffer();
    stylesheet.modelRepr(buf);
    System.out.println(buf.toString());
    System.out.println();
    System.out.println(stylesheet.repr());
  }

//  @Test
  public void testOld() throws Exception {
    String fileName;
    String source;

    fileName = "bug9.less";
    fileName = "bug10.less";
    fileName = "e5.less";
    fileName = "src/test/resources/test-suite/less/color.less";

    source = LessUtils.readFile(Paths.get(fileName));
    LessContext ctx = new LessContext();
    LessStream stm = new LessStream(ctx, source);
    Stylesheet stylesheet = (Stylesheet) stm.parse(Parselets.STYLESHEET);

    Buffer buf = ctx.newBuffer();
    stylesheet.modelRepr(buf);
    System.out.println(buf.toString());
    System.out.println();
    System.out.println(stylesheet.repr());
  }

  static class Case {
    public final String source;
    public final LessSyntax syntax;
    public Case(String source, LessSyntax syntax) {
      this.source = source;
      this.syntax = syntax;
    }
  }

  private static Case make(String source, LessSyntax syntax) {
    return new Case(source, syntax);
  }

//  @Test
  public void testBasic() {
    LessSyntax focus = null;

//    focus = LessSyntax.COLOR;
//     focus = LessSyntax.EXPRESSION;
//    focus = LessSyntax.CONDITIONS;
//    focus = LessSyntax.GUARD;
//    focus = LessSyntax.ELEMENT;
//    focus = LessSyntax.SELECTOR;
//    focus = LessSyntax.RULESET;
//    focus = LessSyntax.RULE;
//    focus = LessSyntax.MIXIN;
//    focus = LessSyntax.DIRECTIVE;
//    focus = LessSyntax.FONT;
//    focus = LessSyntax.SHORTHAND;
//    focus = LessSyntax.MIXIN_CALL;
//    focus = LessSyntax.MIXIN_PARAMS;
//    focus = LessSyntax.URL;
//    focus = LessSyntax.JAVASCRIPT;

    List<Case> cases = Arrays.asList(
//        make("1 + 2", LessSyntax.ADDITION),
//        make("1 - 2", LessSyntax.ADDITION),
//        make("1-2", LessSyntax.ADDITION),
//        make("1 -2", LessSyntax.ADDITION),
//        make("3 - ( /* foo */ 2 * 7 )", LessSyntax.ADDITION),
//        make("1 ! ", LessSyntax.ADDITION), // FAIL
//        make("1 + ", LessSyntax.ADDITION), // FAIL
//        make("x + y ", LessSyntax.ADDITION), // FAIL
//        make("1 + y ", LessSyntax.ADDITION), // FAIL
//
//        make("opacity=2)", LessSyntax.ALPHA),
//        make("opacity=@foo)", LessSyntax.ALPHA),
//        make("opacity=)", LessSyntax.ALPHA),
//        make("opacity=2px)", LessSyntax.ALPHA),
//        make("opacity=2 \n )", LessSyntax.ALPHA),
//        make("opacity=", LessSyntax.ALPHA), // FAIL
//        make("opacity=FOO)", LessSyntax.ALPHA), // FAIL
//        make("x", LessSyntax.ALPHA), // FAIL

//        make("foo=123", LessSyntax.ASSIGNMENT),
//        make("foo = 123", LessSyntax.ASSIGNMENT),
//        make("foo", LessSyntax.ASSIGNMENT), // FAIL
//        make("foo=", LessSyntax.ASSIGNMENT), // FAIL
//        make("foo ! 123", LessSyntax.ASSIGNMENT), // FAIL
//        make("foo=", LessSyntax.ASSIGNMENT), // FAIL
//        make("foo=!", LessSyntax.ASSIGNMENT), // FAIL
//        make("!foo=123", LessSyntax.ASSIGNMENT), // FAIL
//        make("123=123", LessSyntax.ASSIGNMENT), // FAIL

//        make("#abc123", LessSyntax.COLOR),
//        make("#123", LessSyntax.COLOR),
//        make("#11", LessSyntax.COLOR), // FAIL
//        make("11", LessSyntax.COLOR), // FAIL
//        make("#0000", LessSyntax.COLOR), // FAIL
//        make("#00000", LessSyntax.COLOR), // FAIL
//        make("#0000000", LessSyntax.COLOR), // FAIL

//        make("green", LessSyntax.COLOR_KEYWORD),
//        make("+", LessSyntax.COLOR_KEYWORD), // FAIL
//        make("gree", LessSyntax.COLOR_KEYWORD), // FAIL

//        make("// foo", LessSyntax.COMMENT),
//        make("// foo\n", LessSyntax.COMMENT),
//        make("/* foo *", LessSyntax.COMMENT),
//        make("/*! foo */", LessSyntax.COMMENT),
//        make("/* \n foo \n */\n", LessSyntax.COMMENT),
//        make("/", LessSyntax.COMMENT), // FAIL
//        make("/x", LessSyntax.COMMENT), // FAIL
//        make("/*", LessSyntax.COMMENT), // FAIL
//        make("x", LessSyntax.COMMENT), // FAIL
//        make("", LessSyntax.COMMENT), // FAIL

//        make("(@a = 1)", LessSyntax.CONDITION),
//        make("(@a < 1)", LessSyntax.CONDITION),
//        make("(foo = bar)", LessSyntax.CONDITION),
//        make("(@ = 'bar')", LessSyntax.CONDITION),
//        make(" not ( @a = 1 ) ", LessSyntax.CONDITION),
//        make(" not (@a * 1) ", LessSyntax.CONDITION),
//        make("(@a =", LessSyntax.CONDITION), // FAIL
//        make(" not @a = 1 ", LessSyntax.CONDITION), // FAIL
//        make("(@a =. 1)", LessSyntax.CONDITION), // FAIL
//        make("(@a == 1) ", LessSyntax.CONDITION), // FAIL
//        make("(@a ^ 1) ", LessSyntax.CONDITION), // FAIL
//        make("(. = .) ", LessSyntax.CONDITION), // FAIL

//        make("(@a = 1) and (@b = 2)", LessSyntax.CONDITIONS),
//        make("(@a = 1) {", LessSyntax.CONDITIONS),
//        make(" not foo = 1 and bar = 2", LessSyntax.CONDITIONS), // FAIL

//        make("@foo: red", LessSyntax.DEFINITION),
//        make("@foo: ;", LessSyntax.DEFINITION),
//        make("@foo: }", LessSyntax.DEFINITION),
//        make("@foo: 123;", LessSyntax.DEFINITION),
//        make("@foo: 123}", LessSyntax.DEFINITION),
//        make("@123", LessSyntax.DEFINITION),
//        make("@!x", LessSyntax.DEFINITION),
//        make("xyz", LessSyntax.DEFINITION), // FAIL
//        make("@xyz  ", LessSyntax.DEFINITION), // FAIL
//        make("@xyz  :", LessSyntax.DEFINITION), // FAIL
//        make("@xyz  !", LessSyntax.DEFINITION), // FAIL
//        make("@xyz  :   123   !", LessSyntax.DEFINITION), // FAIL

//        make("-12.57px", LessSyntax.DIMENSION),
//        make("123.456", LessSyntax.DIMENSION),
//        make("+3.14159", LessSyntax.DIMENSION),
//        make(".5", LessSyntax.DIMENSION),

//        make("@media { }", LessSyntax.DIRECTIVE),
//        make("@media c and d, e { }", LessSyntax.DIRECTIVE),
//        make("@media (min-width: 48em) { }", LessSyntax.DIRECTIVE),
//        make("@media   foo  and (  width  :  12px  ) {", LessSyntax.DIRECTIVE),
//        make("@media   foo  and (  width  :  12px  ) {", LessSyntax.DIRECTIVE),

        make("foo", LessSyntax.ELEMENT),
        make("*", LessSyntax.ELEMENT),
        make("&", LessSyntax.ELEMENT),
        make("[name='bar']", LessSyntax.ELEMENT),
        make("[name=]", LessSyntax.ELEMENT),

        make("1 foo 2/3 bar 'baz' #123", LessSyntax.EXPRESSION),
        make("1 / 2 /* foo */ bar", LessSyntax.EXPRESSION),
        make("/ 2 3 4", LessSyntax.EXPRESSION),

        make("foo, bar, baz", LessSyntax.EXPRESSION_LIST),
        make("foo  ", LessSyntax.EXPRESSION_LIST),
        make("foo, @a, 1.2px, 'bar', foo(a=1), /** foo **/, 8/3, (3 * 7)", LessSyntax.EXPRESSION_LIST),
        make("!", LessSyntax.EXPRESSION_LIST), // FAIL
        make("foo !", LessSyntax.EXPRESSION_LIST),
        make("foo , !", LessSyntax.EXPRESSION_LIST),

        make("400 12px / 14px", LessSyntax.FONT),
        make("bold 14px/1.5 Helvetica, sans-serif", LessSyntax.FONT),
        make("italic small-caps bold 1em/140% Helvetica, sans-serif", LessSyntax.FONT),
        make("italic, sans-serif, small-caps, bold", LessSyntax.FONT),

        make("foo(a=1)", LessSyntax.FUNCTION_CALL),
        make("foo('bar')", LessSyntax.FUNCTION_CALL),
        make("foo('bar', 'baz', 1, 2, 3)", LessSyntax.FUNCTION_CALL),
        make("url('bar')", LessSyntax.FUNCTION_CALL),
        make("alpha(opacity=0.3)", LessSyntax.FUNCTION_CALL),
        make("alpha(xyz=1)", LessSyntax.FUNCTION_CALL),
        make("foo(", LessSyntax.FUNCTION_CALL), // FAIL
        make("1foo()", LessSyntax.FUNCTION_CALL), // FAIL

        make("when (@a = 1) and (@b = 2), (@c = 3)", LessSyntax.GUARD),
        make("when", LessSyntax.GUARD), // FAIL
        make("when x", LessSyntax.GUARD), // FAIL
        make("when !", LessSyntax.GUARD), // FAIL

        make("`window`", LessSyntax.JAVASCRIPT),

        make("red", LessSyntax.KEYWORD),
        make("-foo", LessSyntax.KEYWORD),
        make("!123", LessSyntax.KEYWORD), // FAIL


//        make(".mixin() { /* foo */ }", LessSyntax.MIXIN),
//        make(".mixin(@a: 1, @b: 2) { }", LessSyntax.MIXIN),
        make(".m(@a, @b) when (@a > 1) { color:#f00; }", LessSyntax.MIXIN),

        make(".mixin(1, 2, 3);", LessSyntax.MIXIN_CALL),
        make(".mixin(1, 2, 3) !important;", LessSyntax.MIXIN_CALL),
        make(".mixin-0(   );", LessSyntax.MIXIN_CALL),

        make("(@a: 1, @b ...)", LessSyntax.MIXIN_PARAMS),

        make("2 * 3", LessSyntax.MULTIPLICATION),
        make("2*3", LessSyntax.MULTIPLICATION),
        make("2*", LessSyntax.MULTIPLICATION),
        make("2/3", LessSyntax.MULTIPLICATION),
        make("2", LessSyntax.MULTIPLICATION),
        make("2^3", LessSyntax.MULTIPLICATION),
        make("2/* foo */ + 4", LessSyntax.MULTIPLICATION),
        make("2// foo\n + 4", LessSyntax.MULTIPLICATION),
        make("2 * !", LessSyntax.MULTIPLICATION), // FAIL

        make("-12px", LessSyntax.OPERAND),
        make("-@foo", LessSyntax.OPERAND),
        make("-(12px + 14px)", LessSyntax.OPERAND),
        make("x", LessSyntax.OPERAND), // FAIL

        make("(2)", LessSyntax.OPERAND_SUB),
        make("(2 * 9)", LessSyntax.OPERAND_SUB),
        make("(  2  +  3  )", LessSyntax.OPERAND_SUB),
        make("(  2  +  3  ", LessSyntax.OPERAND_SUB),
        make("(  2  +  3  !", LessSyntax.OPERAND_SUB), // FAIL
        make("x", LessSyntax.OPERAND_SUB), // FAIL
        make("()", LessSyntax.OPERAND_SUB), // FAIL
        make("(2 * 9", LessSyntax.OPERAND_SUB), // FAIL
        make("(", LessSyntax.OPERAND_SUB), // FAIL

        make("@foo: 1", LessSyntax.PARAMETER),
        make("@foo ...", LessSyntax.PARAMETER),
        make("123", LessSyntax.PARAMETER),
        make("!", LessSyntax.PARAMETER),
        make("@!", LessSyntax.PARAMETER),
        make("@@@foo", LessSyntax.PARAMETER),
        make("@foo !", LessSyntax.PARAMETER),
        make("@foo .!", LessSyntax.PARAMETER),
        make("@foo ..!", LessSyntax.PARAMETER),

        make("'foo bar'", LessSyntax.QUOTED),
        make("'foo", LessSyntax.QUOTED),
        make("'foo \\n bar'", LessSyntax.QUOTED),
        make("'foo \\", LessSyntax.QUOTED),
        make("~\"baz biz\"", LessSyntax.QUOTED),
        make("'a var @{foo} is here'", LessSyntax.QUOTED),
        make("'@{foo}'", LessSyntax.QUOTED),
        make("'foo \\@ bar'", LessSyntax.QUOTED),
        make("'foo @. @{foo}'", LessSyntax.QUOTED),
        make("'@.'", LessSyntax.QUOTED),
        make("'foo \\", LessSyntax.QUOTED),
        make("'foo\nbar'", LessSyntax.QUOTED), // FAIL
        make("x", LessSyntax.QUOTED), // FAIL

        make("1/5", LessSyntax.RATIO),
        make("1/x", LessSyntax.RATIO), // FAIL

//        make("foo: bar;", LessSyntax.RULE),
//        make("font-size: 14rem;", LessSyntax.RULE),
//        make("color: #111;", LessSyntax.RULE),
//        make("color  :  #111  ;", LessSyntax.RULE),
//        make("color: ;", LessSyntax.RULE),
//        make("font-size: 12px !important ;", LessSyntax.RULE),
//        make("foo: bar baz /* foo */ baz;", LessSyntax.RULE),
//        make("foo: #0000;", LessSyntax.RULE),
//        make("foo: #00000;", LessSyntax.RULE),
//        make("foo: #00000000;", LessSyntax.RULE),
//        make("display: none;\n", LessSyntax.RULE),
//        make("a1: url(http://squarespace.com);", LessSyntax.RULE),
//        make("b5: (4 / .5);", LessSyntax.RULE),
//        make("background: url(some-image.jpg) no-repeat right .75rem center / .8rem 1rem;", LessSyntax.RULE),
//        make("!foo: bar", LessSyntax.RULE), // FAIL
//        make("foo", LessSyntax.RULE), // FAIL
//        make("foo:", LessSyntax.RULE), // FAIL
//        make("foo !", LessSyntax.RULE), // FAIL
        make("foo: `window`;", LessSyntax.RULE),

        make("foo, bar { color: red; }", LessSyntax.RULESET),
        make("foo, bar { color: red }", LessSyntax.RULESET),
        make("foo, bar { color: red; baz { font-size: 16px; } }", LessSyntax.RULESET),

        make("(~'@{foo}')", LessSyntax.SELECTOR),

        make("small/12px", LessSyntax.SHORTHAND),
        make("foo/3.14", LessSyntax.SHORTHAND),

        make("\n// foo\n\n/*\n bar \n*/\n", LessSyntax.STYLESHEET),
        make("color: red; font-size: 12px", LessSyntax.STYLESHEET),

        make("U+?", LessSyntax.UNICODE_RANGE),
        make("U.", LessSyntax.UNICODE_RANGE), // FAIL

        make("url(https://foo/bar)", LessSyntax.URL),
        make("url('https://google.com')", LessSyntax.URL),
        make("url(@foo)", LessSyntax.URL),
        make("xurl(https://google.com)", LessSyntax.URL), // FAIL
        make("url(https://google.com", LessSyntax.URL), // FAIL

        make("@@foo-bar", LessSyntax.VARIABLE),
        make("@!", LessSyntax.VARIABLE), // FAIL

        make("@{foo}", LessSyntax.VARIABLE_CURLY),
        make("@@{foo}", LessSyntax.VARIABLE_CURLY),
        make("@{foo", LessSyntax.VARIABLE_CURLY), // FAIL

        // =========== TEST ========

        // REMOVE ME
        make("end", LessSyntax.KEYWORD)
    );

    for (Case c : cases) {
      if (focus != null && c.syntax != focus) {
        continue;
      }
      System.out.println("Parsing: \"" + StringEscapeUtils.escapeJava(c.source) + "\"");

      LessContext ctx = new LessContext();
      LessParser p = new LessParser(ctx, c.source);
      Node r = null;
      try {
        r = p.parse(c.syntax);
      } catch (Exception e) {
        System.out.println("ERROR:");
        e.printStackTrace(System.out);
        System.out.println("===============================================");
        continue;
      }

      if (r == null) {
        System.out.println("<null>");
      } else {
        Buffer buf = new Buffer(2);
        System.out.println("representation: " + r.repr());
        r.modelRepr(buf);
        System.out.println(buf.toString());
      }
      System.out.println(p);
      System.out.println("===============================================");
    }
  }
}
