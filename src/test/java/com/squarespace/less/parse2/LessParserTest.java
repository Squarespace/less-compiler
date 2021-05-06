package com.squarespace.less.parse2;

import static com.squarespace.less.SyntaxErrorType.ALPHA_UNITS_INVALID;
import static com.squarespace.less.SyntaxErrorType.EXPECTED;
import static com.squarespace.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.less.SyntaxErrorType.JAVASCRIPT_DISABLED;
import static com.squarespace.less.SyntaxErrorType.MIXED_DELIMITERS;
import static com.squarespace.less.SyntaxErrorType.QUOTED_BARE_LF;
import static com.squarespace.less.model.Operator.ADD;

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
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Operator;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Unit;

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

    // BUG4
    t.ok("1 + ", dim(1));

    t.fail("1 -2", INCOMPLETE_PARSE);
    t.fail("1 ! ", INCOMPLETE_PARSE);
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
    Tester t = tester(LessSyntax.COLOR);

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
    Tester t = tester(LessSyntax.COLOR_KEYWORD);

    t.ok("green", color("green"));

    t.fail("+", INCOMPLETE_PARSE);
    t.fail("gree", INCOMPLETE_PARSE);
  }

  @Test
  public void testComments() throws LessException {
    Tester t = tester(LessSyntax.COMMENT);

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
    Tester t = tester(LessSyntax.CONDITION);

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
    Tester t = tester(LessSyntax.CONDITIONS);

    t.ok("(@a = 1) and (@b >= 2)",
        cond(Operator.AND,
            cond(Operator.EQUAL, var("@a"), dim(1)),
            cond(Operator.GREATER_THAN_OR_EQUAL, var("@b"), dim(2))));

    t.ok("(@a = 1)", cond(Operator.EQUAL, var("@a"), dim(1)));

    t.fail(" not foo = 1 and (bar = 2)", EXPECTED);
  }

  @Test
  public void testDefinition() throws LessException {
    Tester t = tester(LessSyntax.DEFINITION);

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
    Tester t = tester(LessSyntax.DIMENSION);

    t.ok("-12.57px", dim(-12.57, Unit.PX));
    t.ok(".5", dim(0.5));
    t.ok("+3.14159", dim(3.14159));

    t.fail("ab", INCOMPLETE_PARSE);
    t.fail("15xyz", INCOMPLETE_PARSE); // unknown unit
  }

  @Test
  public void testDirective() throws LessException {
    Tester t = tester(LessSyntax.DIRECTIVE);

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

  @Test
  public void testElement() throws LessException {
    Tester t = tester(LessSyntax.ELEMENT);

    t.ok("1%", element("1%"));
    t.ok("1.5%", element("1.5%"));
    t.ok("foo", element("foo"));
    t.ok(">foo", element(Combinator.CHILD, "foo"));
    t.ok("#name-space", element("#name-space"));
    t.ok("(a-b-c)", element("(a-b-c)"));
    t.ok("@{var}", element(Combinator.DESC, var("@var", true)));
    t.ok(".class-name", element(".class-name"));
    t.ok("*", element("*"));
    t.ok("&", element("&"));
    t.ok("[name=]", attr(Combinator.DESC, anon("name")));

    t.ok("[name=", null); // entire string is parsed, so complete, but invalid so returns null

    t.ok("[name='bar']",
        attr(Combinator.DESC,
            anon("name"),
            anon("="),
            quoted('\'', false, anon("bar"))));

    t.fail("!~", INCOMPLETE_PARSE);
  }

  @Test
  public void testExpression() throws LessException {
    Tester t = tester(LessSyntax.EXPRESSION);

    t.ok("1 foo 2/3 bar 'baz' #123",
        expn(
            dim(1),
            kwd("foo"),
            oper(Operator.DIVIDE, dim(2), dim(3)),
            kwd("bar"),
            quoted('\'', false, "baz"),
            color("#123")));

    t.ok("1 / 2 /* foo */ bar",
        expn(
            oper(Operator.DIVIDE, dim(1), dim(2)),
            comment(" foo ", true),
            kwd("bar")));

    t.ok("1 2 /* foo */",
        expn(dim(1), dim(2), comment(" foo ", true)));

    t.ok("1 // foo\n",
        expn(dim(1), comment(" foo", false)));

    t.ok("/* foo */ 1 2 3\n",
        expn(comment(" foo ", true), dim(1), dim(2), dim(3)));

    t.ok("foo bar",
        expn(kwd("foo"), kwd("bar")));

    t.ok("foo", kwd("foo")); // automatic flattening
    t.ok("foo  ", kwd("foo"));

    t.ok("foo / bar",
        expn(kwd("foo"), anon("/"), kwd("bar")));

    t.ok("/* bar */// foo\n",
        expn(comment(" bar ", true), comment(" foo", false)));

    t.ok("", null);

    t.fail("!foo", INCOMPLETE_PARSE);
    t.fail("foo \n !", INCOMPLETE_PARSE);
  }

  @Test
  public void testExpressionList() throws LessException {
    Tester t = tester(LessSyntax.EXPRESSION_LIST);

    t.ok("foo,   ", kwd("foo")); // automatic flattening

    t.ok("foo, bar, baz", expnlist(kwd("foo"), kwd("bar"), kwd("baz")));
    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("foo !", INCOMPLETE_PARSE);
    t.fail("foo , !", INCOMPLETE_PARSE);
  }

  @Test
  public void testFont() throws LessException {
    Tester t = tester(LessSyntax.FONT);

    t.ok("400 12px / 14px",
        expnlist(
            expn(dim(400), dim(12, Unit.PX), anon("/"), dim(14, Unit.PX))));

    t.ok("bold 14px/1.5 Helvetica, sans-serif",
        expnlist(
            expn(
                kwd("bold"),
                shorthand(dim(14, Unit.PX), dim(1.5)),
                kwd("Helvetica")),
            kwd("sans-serif")));

    t.ok("400 /* foo */ bold",
        expnlist(
            expn(dim(400), comment(" foo ", true), kwd("bold"))));

    t.ok("italic, sans-serif, small-caps, bold",
        expnlist(
            expn(kwd("italic")),
            kwd("sans-serif"),
            kwd("small-caps"),
            kwd("bold")));

    t.ok("", expnlist(expn()));

    t.fail("!", INCOMPLETE_PARSE);
  }

  @Test
  public void testFunctionCall() throws LessException {
    Tester t = tester(LessSyntax.FUNCTION_CALL);

    t.ok("", null);
    t.ok("foo(a=1)", call("foo", assign("a", dim(1))));
    t.ok("foo( a=1 )", call("foo", assign("a", dim(1))));

    t.ok("foo('bar')", call("foo", quoted('\'', false, "bar")));
    t.ok("foo('bar', ~'baz', 12px)",
        call("foo",
            quoted('\'', false, "bar"),
            quoted('\'', true, "baz"),
            dim(12, Unit.PX)));

    t.ok("url(https://example.com)", url(anon("https://example.com")));
    t.ok("url('https://example.com')", url(quoted('\'', false, "https://example.com")));

    t.ok("alpha(1,2,3)", call("alpha", dim(1), dim(2), dim(3)));

    t.ok("alpha(OPACITY=1.7)", alpha(dim(1.7)));
    t.ok("alpha(opacity=@opacity)", alpha(var("@opacity")));

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("foo(", INCOMPLETE_PARSE);
    t.fail("foo ()", INCOMPLETE_PARSE); // keyword followed by parens
    t.fail("foo(!", INCOMPLETE_PARSE);

    t.fail("alpha(opacity=12px)", ALPHA_UNITS_INVALID);
  }

  @Test
  public void testGuard() throws LessException {
    Tester t = tester(LessSyntax.GUARD);

    t.ok("when (@a = 1) and (@b = 2), (@c = 3)",
        guard(
            cond(Operator.AND,
                cond(Operator.EQUAL, var("@a"), dim(1)),
                cond(Operator.EQUAL, var("@b"), dim(2))),
            cond(Operator.EQUAL, var("@c"), dim(3))));


    t.ok("", null);
    t.fail("when (@a = 1), !", EXPECTED);
    t.fail("when x", EXPECTED);
    t.fail("!", INCOMPLETE_PARSE);
    t.fail("WHEN (@a = 1)", INCOMPLETE_PARSE);
  }


  @Test
  public void testJavascript() throws LessException {
    Tester t = tester(LessSyntax.JAVASCRIPT);

    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("`window != null`", JAVASCRIPT_DISABLED);
    t.fail("~`window != @var`", JAVASCRIPT_DISABLED);
  }

  @Test
  public void testKeyword() throws LessException {
    Tester t = tester(LessSyntax.KEYWORD);

    t.ok("red", color("red"));
    t.ok("long-word-keyword", kwd("long-word-keyword"));
    t.ok("-foo", kwd("-foo"));
    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
  }

  @Test
  public void testMixin() throws LessException {
    Tester t = tester(LessSyntax.MIXIN);

    t.ok("#m() { }", mixin("#m", params(), null));
    t.ok(".m(@a, @b) when (@a > 1) { }",
        mixin(".m",
            params(param("@a"), param("@b")),
            guard(cond(Operator.GREATER_THAN, var("@a"), dim(1)))));

    t.ok("", null);

    t.fail(".m() !", INCOMPLETE_PARSE);
    t.fail(".m  !", INCOMPLETE_PARSE);
    t.fail(">foo()", INCOMPLETE_PARSE);
  }

  @Test
  public void testMixinCall() throws LessException {
    Tester t = tester(LessSyntax.MIXIN_CALL);

    t.ok(".mixin(1, 2);",
        mixincall(
            selector(element(null, ".mixin")),
            args(',', arg(dim(1)), arg(dim(2)))));

    t.ok("#ns > .mixin(@a: 1, @b: @c - 1)",
        mixincall(
            selector(element(null, "#ns"), element(Combinator.CHILD, ".mixin")),
            args(',', arg("@a", dim(1)), arg("@b", oper(Operator.SUBTRACT, var("@c"), dim(1))))));

    t.ok(".a > .b > .c (@foo; @bar) !important ;",
        mixincall(
            selector(
                element(null, ".a"),
                element(Combinator.CHILD, ".b"),
                element(Combinator.CHILD, ".c")),
            args(';', arg(var("@foo")), arg(var("@bar"))),
            true));

    t.ok(".mixin;", mixincall(selector(element(null, ".mixin"))));
    t.ok(".mixin  (  1  )  ;",
        mixincall(
            selector(element(null, ".mixin")),
            args(',', arg(dim(1)))));

    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
    t.fail(".mixin!;", INCOMPLETE_PARSE);
    t.fail(".mixin(@a: !)", EXPECTED);
    t.fail(".mixin(@a, @b, ", EXPECTED);
    t.fail(".mixin(@a, @b, @c", EXPECTED);
    t.fail(".mixin(1, 2, @a: 3, 4; 5);", MIXED_DELIMITERS);
  }

  @Test
  public void testMixinParams() throws LessException {
    Tester t = tester(LessSyntax.MIXIN_PARAMS);

    t.ok("(@a: 1, @b ...)",
        params(
            param("@a", dim(1)),
            param("@b", true)));

    t.ok("(...)",
        params(param(null, true)));

    t.ok("", null);

    t.fail("(1, 2, 3 !", INCOMPLETE_PARSE);
    t.fail("(", INCOMPLETE_PARSE);
    t.fail("(.", INCOMPLETE_PARSE);
    t.fail("(..", INCOMPLETE_PARSE);
  }

  @Test
  public void testMultiplication() throws LessException {
    Tester t = tester(LessSyntax.MULTIPLICATION);

    t.ok("2 * 3", oper(Operator.MULTIPLY, dim(2), dim(3)));
    t.ok("2*3", oper(Operator.MULTIPLY, dim(2), dim(3)));
    t.ok("2/3", oper(Operator.DIVIDE, dim(2), dim(3)));
    t.ok("2", dim(2));

    t.fail("2^3", INCOMPLETE_PARSE);
    t.fail("2 /* foo */ / 3", INCOMPLETE_PARSE);
    t.fail("2 * !", INCOMPLETE_PARSE);

    // TODO: increase coverage

    // TODO: the following should fail
//    t.fail("2*", INCOMPLETE_PARSE);

  }

  @Test
  public void testOperand() throws LessException {
    Tester t = tester(LessSyntax.OPERAND);

    t.ok("-12px", dim(-12, Unit.PX));

    t.ok("-@foo", oper(Operator.MULTIPLY, var("@foo"), dim(-1)));

    t.ok("-(12px + 14px)",
        oper(Operator.MULTIPLY,
            oper(Operator.ADD, dim(12, Unit.PX), dim(14, Unit.PX)),
            dim(-1)));

    t.ok("foo(1, 2)", call("foo", dim(1), dim(2)));
    t.ok("green", color("green"));
    t.ok("#123", color("#123"));

    t.ok("( 1 2 3 )", expn(dim(1), dim(2), dim(3)));

    t.ok("", null);

    t.fail("x", INCOMPLETE_PARSE);
    t.fail("(", INCOMPLETE_PARSE);
    t.fail("(())", INCOMPLETE_PARSE);
    t.fail("( 1 2", INCOMPLETE_PARSE);
    t.fail("( 1 2 !", INCOMPLETE_PARSE);
  }

  @Test
  public void testOperandSub() throws LessException {
    Tester t = tester(LessSyntax.OPERAND_SUB);

    t.ok("(2)", dim(2));
    t.ok("( 2 * 9 )", oper(Operator.MULTIPLY, dim(2), dim(9)));

    t.ok("", null);

    t.fail("(", INCOMPLETE_PARSE);
    t.fail("(())", INCOMPLETE_PARSE);
    t.fail("( 1 2 ", INCOMPLETE_PARSE);
    t.fail("( 1 2 !", INCOMPLETE_PARSE);
  }

  @Test
  public void testParameter() throws LessException {
    Tester t = tester(LessSyntax.PARAMETER);

    t.ok("@foo: 1", param("@foo", dim(1)));
    t.ok("@foo ...", param("@foo", true));
    t.ok("123", param(null, dim(123)));
    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("@foo !", INCOMPLETE_PARSE);
    t.fail("@foo .!", INCOMPLETE_PARSE);
    t.fail("@foo ..!", INCOMPLETE_PARSE);
    t.fail("'foo' \\", INCOMPLETE_PARSE);
    t.fail("@!", INCOMPLETE_PARSE);
    t.fail("@foo: !", EXPECTED);
  }

  @Test
  public void testQuoted() throws LessException {
    Tester t = tester(LessSyntax.QUOTED);

    t.ok("'foo bar'", quoted('\'', false, "foo bar"));
    t.ok("'foo", quoted('\'', false, "foo"));
    t.ok("~\"baz biz\"", quoted('"', true, "baz biz"));
    t.ok("'foo \\", quoted('\'', false, "foo \\"));
    t.ok("'a var @{foo} is here'", quoted('\'', false, anon("a var "), var("@foo", true), anon(" is here")));
    t.ok("'@{foo}'", quoted('\'', false, var("@foo", true)));
    t.ok("'@.'", quoted('\'', false, "@."));
    t.ok("'foo @. @{foo}'", quoted('\'', false, anon("foo @. "), var("@foo", true)));
    t.ok("'foo \\n bar'", quoted('\'', false, "foo \\n bar"));

    t.ok("", null);

    t.fail("x", INCOMPLETE_PARSE);
    t.fail("'foo\nbar'", QUOTED_BARE_LF);
  }

  @Test
  public void testRatio() throws LessException {
    Tester t = tester(LessSyntax.RATIO);

    t.ok("1/5", ratio("1/5"));
    t.ok("", null);

    t.fail("1/", INCOMPLETE_PARSE);
    t.fail("1/x", INCOMPLETE_PARSE);
  }

  @Test
  public void testRule() throws LessException {
    Tester t = tester(LessSyntax.RULE);

    t.ok("foo: bar;", rule(prop("foo"), kwd("bar")));
    t.ok("foo:", rule(prop("foo"), anon("")));
    t.ok("foo : #123 !important ;", rule(prop("foo"), color("#123"), true));
    t.ok("font: sans-serif;", rule(prop("font"), expnlist(expn(kwd("sans-serif")))));
    t.ok("color: #xyz;", rule(prop("color"), anon("#xyz")));
    t.ok("color: #xyz !important ;", rule(prop("color"), anon("#xyz !important")));

    // BUG3
    t.ok("foo: @foo();", rule(prop("foo"), var("@foo", false)));

    // BUG4
    t.ok("font-size: random(98) + px;",
        rule(prop("font-size"), expn(call("random", dim(98)), kwd("px"))));

    t.ok("font-size: 1 + px;",
        rule(prop("font-size"), expn(dim(1), kwd("px"))));

    t.ok("", null);

    t.fail("foo!", INCOMPLETE_PARSE);
    t.fail("foo: bar!", INCOMPLETE_PARSE);
  }

  @Test
  public void testRuleset() throws LessException {
    Tester t = tester(LessSyntax.RULESET);

    Ruleset r = ruleset(selector(element("foo")), selector(element("bar")));
    r.add(rule(prop("color"), color("red")));

    t.ok("foo, bar { color: red; }", r);

    Ruleset r2 = ruleset(selector(element("baz")));
    r2.add(rule(prop("font-size"), dim(16, Unit.PX)));

    Ruleset r1 = ruleset(selector(element("foo")), selector(element("bar")));
    r1.add(rule(prop("color"), color("red")));
    r1.add(r2);

    t.ok("foo, bar { color: red; baz { font-size: 16px; } }", r1);
    t.ok("", null);

    // skip extraneous semicolons
    t.ok("a { ; ; ; ; }", ruleset(selector(element("a"))));

    t.fail("!", INCOMPLETE_PARSE);
    t.fail(".foo ", INCOMPLETE_PARSE);
    t.fail(".foo {\n", INCOMPLETE_PARSE);
  }

  @Test
  public void testSelector() throws LessException {
    Tester t = tester(LessSyntax.SELECTOR);

    t.ok(":nth-child(@{num})",
        selector(
          element(":nth-child"),
          element(null, paren(var("@num", true)))));

    t.ok("(~'@{foo}')",
        selector(element(Combinator.DESC, quoted('\'', true, var("@foo", true)))));

    t.ok("baz > quux", selector(element("baz"), element(Combinator.CHILD, "quux")));

    t.ok("", null);

    t.fail("!", INCOMPLETE_PARSE);
    t.fail("a!", INCOMPLETE_PARSE);
    t.fail("( a ", INCOMPLETE_PARSE);
  }

  @Test
  public void testShorthand() throws LessException {
    Tester t = tester(LessSyntax.SHORTHAND);

    t.ok("small/12px", shorthand(kwd("small"), dim(12, Unit.PX)));
    t.ok("foo/3.14", shorthand(kwd("foo"), dim(3.14)));

    t.ok("", null);

    t.fail("small", INCOMPLETE_PARSE);
    t.fail("small!", INCOMPLETE_PARSE);
    t.fail("!", INCOMPLETE_PARSE);
    t.fail("!/!", INCOMPLETE_PARSE);
    t.fail("small / 12px", INCOMPLETE_PARSE);
  }

  @Test
  public void testStylesheet() throws LessException {
    Tester t = tester(LessSyntax.STYLESHEET);

    t.ok("", stylesheet());
    t.ok("\n// foo\n\n/*\n bar \n*/\n",
        stylesheet(comment(" foo", false), comment("\n bar \n", true)));

    t.ok("color: red; font-size: 12px",
        stylesheet(
            rule(prop("color"), color("red")),
            rule(prop("font-size"), dim(12, Unit.PX))));
  }

  @Test
  public void testUnicodeRange() throws LessException {
    Tester t = tester(LessSyntax.UNICODE_RANGE);

    t.ok("U+?", unicode("U+?"));
    t.ok("", null);

    t.fail("Ux", INCOMPLETE_PARSE);
  }

  @Test
  public void testVariable() throws LessException {
    Tester t = tester(LessSyntax.VARIABLE);

    t.ok("@@foo-bar", var("@@foo-bar", false));
    t.ok("", null);

    t.fail("@!", INCOMPLETE_PARSE);
  }

  @Test
  public void testVariableCurly() throws LessException {
    Tester t = tester(LessSyntax.VARIABLE_CURLY);

    t.ok("@{foo}", var("@foo", true));
    t.ok("@@{foo}", var("@@foo", true));
    t.ok("", null);

    t.fail("@!", INCOMPLETE_PARSE);
    t.fail("@{f", INCOMPLETE_PARSE);
    t.fail("@{!}", INCOMPLETE_PARSE);
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

  private static Tester tester(LessSyntax syntax) {
    return new Tester(syntax);
  }

}
