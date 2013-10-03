package com.squarespace.v6.template.less.exec;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessCompiler;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.LessUtils;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


/**
 * Runs the on-disk test suite.
 */
public class LessSuiteTest {

  @Test
  public void testSuite() throws IOException {
    URL top = getClass().getClassLoader().getResource("squarespace-less-suite");
    Path rootPath = Paths.get(top.getPath());
    Path lessRoot = rootPath.resolve("less");
    Path cssRoot = rootPath.resolve("css");
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.less");
    DirectoryStream<Path> dirStream = LessUtils.getMatchingFiles(lessRoot, matcher);
    StringBuilder failures = new StringBuilder();
    for (Path lessPath : dirStream) {

      // Setup the compiler
      Options opts = new Options();
      opts.importRoot(lessRoot.toString());
      
      Context ctx = new Context(opts);
      LessCompiler compiler = new LessCompiler();
      ctx.setCompiler(compiler);

      // Read and compile the .less source
      String source = LessUtils.readFile(lessPath);
      String lessCompiled = null;
      try {
        lessCompiled = compiler.compile(source, ctx);
      } catch (LessException e) {
        failures.append("\nCompile error " + lessPath.getFileName() + "\n" + e.errorInfo().getMessage());
        continue;
      }
      
      // Compare with expected CSS result.
      String[] parts = lessPath.getFileName().toString().split("\\.(?=[^\\.]+$)");
      Path cssPath = cssRoot.resolve(parts[0] + ".css").normalize();
      String cssData = LessUtils.readFile(cssPath);
      
      String result = diff(cssData, lessCompiled);
      if (result != null) {
        failures.append("\nDifferences detected in compiled output for ");
        failures.append(lessPath.getFileName() + "\n" + result);
      }
    }
    
    if (failures.length() > 0) {
      Assert.fail(failures.toString());
    }
  }
  
  /**
   * Create a diff between the expected and actual strings. If any
   * differences are found, format an error message.
   */
  private String diff(String expected, String actual) {
    List<String> expList = Arrays.asList(expected.split("\n"));
    List<String> actList = Arrays.asList(actual.split("\n"));
    Patch<String> patch = DiffUtils.diff(expList, actList);
    List<Delta<String>> deltas = patch.getDeltas();
    if (deltas.size() == 0) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    for (Delta<String> delta : deltas) {
      Chunk<String> chunk1 = delta.getOriginal();
      int pos1 = chunk1.getPosition();
      List<String> lines1 = chunk1.getLines();

      Chunk<String> chunk2 = delta.getRevised();
      int pos2 = chunk2.getPosition();
      List<String> lines2 = chunk2.getLines();

      buf.append("@@ -" + pos1 + "," + lines1.size());
      buf.append(" +" + pos2 + "," + lines2.size()).append(" @@\n");
      for (String row : lines1) {
        buf.append("- ").append(row).append('\n');
      }
      for (String row : lines2) {
        buf.append("+ ").append(row).append('\n');
      }
    }
    return buf.toString();
  }

}
