package com.squarespace.less;

import static org.testng.Assert.assertEquals;

import java.nio.file.Paths;

import org.testng.annotations.Test;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.SyntaxErrorMaker;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessSyntax;

public class LessMessagesTest {

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

    LessMessages messages = new LessMessages(2, 1);
    String[] lines = messages.formatError(exc).split("\n");

    assertEquals(lines[0], "");
    assertEquals(lines[1], "");
    assertEquals(lines[2], "SQUARESPACE_LESS_ERROR");
    assertEquals(lines[3], "");
    assertEquals(lines[4], "Line  Statement");
    assertEquals(lines[5], "---------------");
    assertEquals(lines[6], ":1  .foo .bar {");
    assertEquals(lines[7], "    ");
    assertEquals(lines[8], ".. skipped 1 frames");
    assertEquals(lines[9], "");
    assertEquals(lines[10], ":3      bar: #123123");
  }

  @Test
  public void testDefault() throws LessException {
    LessMessages messages = new LessMessages();
    Buffer buf = new Buffer(2);
    messages.successHeader(buf);
    String[] lines = buf.toString().split("\n");
    assertEquals(lines[1], "/* Squarespace LESS Compiler 1.3.3 */");

  }

  @Test
  public void testStats() throws LessException {
    LessStats stats = new LessStats();
    stats.parseDone(100, 1);

    LessMessages messages = new LessMessages(2, 1);
    String[] lines = messages.formatStats(stats, Paths.get("foo", "bar")).split("\n");

    assertEquals(lines[0], "/* --------------------------------------------------------");
    assertEquals(lines[1], "Squarespace LESS compiler 1.3.3 Statistics for 'foo/bar':");
  }

  private static Node parse(String raw) throws LessException {
    LessParser parser = new LessParser(new LessContext(), raw);
    Node res = parser.parse(LessSyntax.STYLESHEET);
    parser.complete();
    return res;
  }

}
