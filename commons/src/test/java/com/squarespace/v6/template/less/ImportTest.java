package com.squarespace.v6.template.less;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;

// XXX: convert to execute the on-disk test suite.

public class ImportTest extends LessTestBase {

  @Test
  public void testFoo() {
    System.out.println(System.nanoTime() / 1000000.0);
    System.out.println(System.currentTimeMillis());
  }
  
//  @Test
  public void testImport() throws LessException {
    Options opts = new Options();
    opts.importRoot("/Users/phensley");
    LessHarness h = new LessHarness();
    
    String res = h.execute("@import 'foo.less'; .y { b: @b; } @import 'foo.less';", opts);
    System.out.println(res);
  }

//  @Test
  public void testImportCase() throws Exception {
    String root = "/Users/phensley/work/less_work/sqsp-tests";
//    String root = "/Users/phensley/work/less_Work/less.js/test/less";
//  String root = "/Users/phensley/work/less_work/sqsp-tests-2";
    Options opts = new Options();
    opts.importRoot(root);
    Path[] path = new Path[] {
        Paths.get(root, "sqs-scope-4.less")
//        Paths.get(root, "media.less"),
//        Paths.get(root, "css.less"),
//        Paths.get(root, "comments.less")
    };

    int len = path.length;
//    Thread.sleep(12000);
    while (true) {
      int N = 1;
      String res = null;
      for (int i = 0; i < N; i++) {
        String data = readFile(path[i%len]);
        long start = System.nanoTime();
        Context ctx = new Context(opts);
        LessCompiler compiler = new LessCompiler();
        res = compiler.compile(data, ctx);
        System.out.println(res);
        long elapsed = System.nanoTime() - start;
        LessStats stats = ctx.stats();
        System.out.println("    parse time: " + stats.parseTimeMs());
        System.out.println("  compile time: " + stats.compileTimeMs());
        System.out.println("disk wait time: " + stats.diskWaitTimeMs());
        System.out.println("  import count: " + stats.importCount());
        System.out.println("elapsed: " + elapsed / 1000000.0);
        System.out.println("---------------------------------------");
        
      }
      System.out.println("sleeping..");
      Thread.sleep(3000);
    }
//    String data = readFile(path);
//    String res = h.execute(data, opts);
//    String res = h.execute("@import 'foo.less'; .y { b: @b; } @import 'foo.less';", opts);
//    System.out.println(res);
  }

  private String runOnce(String data, LessHarness h, Options opts) throws LessException {
    return h.execute(data, opts);
  }
  
  private String readFile(Path path) {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
    } catch (IOException e) {
      String message = String.format("Failure to read from '%s'", path);
      throw new RuntimeException(message + ": " + e.getMessage(), e);
    }
  }

}
