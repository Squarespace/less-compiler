package com.squarespace.less;

import java.nio.file.Paths;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessUtils;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessSyntax;

public class InternTest {

  @Test
  public void testIntern() throws Exception {
    String path = "/Users/phensley/dev/less-exp/less/new-bedford-framework-canonical.less";
    String source = LessUtils.readFile(Paths.get(path));
//    String source = "a { font-family: 'Helvetica Neue', Helvetica, Sans-serif; }";
    LessCompiler compiler = new LessCompiler();
    LessOptions opts = new LessOptions();
    opts.addImportPath("/Users/phensley/dev/squarespace-v6/site-server/src/main/webapp/universal/styles-v6");
    opts.addImportPath("/Users/phensley/dev/squarespace-v6/site-server/src/main/webapp/templates-v6/system");
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(compiler);
    LessParser parser = new LessParser(ctx, source);
    try {
    parser.parse(LessSyntax.STYLESHEET);
    System.out.println("length = " + source.length());
    double pct = parser.interned_bytes / ((double) source.length());
    System.out.println("percent = " + (100 * pct));
    System.out.println("interned = " + parser.interned);
    System.out.println("  failed = " + parser.failed);
    System.out.println("   bytes = " + parser.interned_bytes);
    System.out.println("    fail = " + parser.interned_fail);
    } catch (LessException e) {
      LessMessages msg = new LessMessages(2, 10);
      System.out.println(msg.formatError(e));
    }
  }

}
