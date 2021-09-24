package com.squarespace.less.core;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessErrorInfo;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessSyntax;

public class StackFormatterTest {

  private static final LessCompiler COMPILER = new LessCompiler();

  @Test
  public void testBasic() throws LessException {
    Stylesheet sheet = (Stylesheet) parse(".foo .bar{\n  .baz {\n  bar: #123123;\n }\n }");
    Ruleset rs1 = (Ruleset) sheet.block().rules().get(0);
    Ruleset rs2 = (Ruleset) rs1.block().rules().get(0);
    Rule rule = (Rule) rs2.block().rules().get(0);


    LessErrorInfo info = SyntaxErrorMaker.expected("foo is bar");
    LessException exc = new LessException(info);
    exc.push(rule);
    exc.push(rs2);
    exc.push(rs1);

    StackFormatter fmt = new StackFormatter(exc.errorContext(), 2, 20);
    String[] lines = fmt.format().split("\n");
    assertEquals(lines[2], ":1  .foo .bar {");
    assertEquals(lines[3], ":2    .baz {");
    assertEquals(lines[4], ":3      bar: #123123");
  }

  @Test
  public void testParse() throws LessException {
    String raw = ".foo {\n  .bar {\n    baz xx____ ...\n";
    try {
      COMPILER.parse(raw, new LessContext());
    } catch (LessException e) {
      StackFormatter fmt = new StackFormatter(e.errorContext(), 2, 20);

      String[] lines = fmt.format().split("\n");
      assertEquals(lines[2], "   1   .foo {");
      assertEquals(lines[3], "   2     .bar {");
      assertEquals(lines[4], "   3       baz xx____ ...");
      assertEquals(lines[5], "       ...............^");
    }
  }

  @Test
  public void testRecursion() throws LessException {
    LessOptions opts = new LessOptions();
    opts.mixinRecursionLimit(20);
    LessContext ctx = new LessContext(opts);
    try {
      COMPILER.compile(".mix() {\n  .mix()\n }\n .foo {\n  .mix()\n  }\n", ctx);
    } catch (LessException e) {
      StackFormatter fmt = new StackFormatter(e.errorContext(), 2, 5);
      String[] lines = fmt.format().split("\n");

      // Recursion of depth 20 allowed, so 21st frame is the source of the error.

      // Show 5 frames
      assertEquals(lines[2], ":4  .foo {");
      assertEquals(lines[3], ":5    .mix();");
      assertEquals(lines[4], ":2      .mix();");
      assertEquals(lines[5], ":2        .mix();");
      assertEquals(lines[6], ":2          .mix();");

      // Skip 11 frames
      // lines[7] blank
      assertEquals(lines[8], ".. skipped 11 frames");
      // lines[9] blank

      // Show last 5 frames
      assertEquals(lines[10], ":2            .mix();");
      assertEquals(lines[11], ":2              .mix();");
      assertEquals(lines[12], ":2                .mix();");
      assertEquals(lines[13], ":2                  .mix();");
      assertEquals(lines[14], ":2                    .mix();");
    }
  }

  private Node parse(String raw) throws LessException {
    LessParser parser = new LessParser(new LessContext(), raw);
    Node res = parser.parse(LessSyntax.STYLESHEET);
    parser.complete();
    return res;
  }

}
