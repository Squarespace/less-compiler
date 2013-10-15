package com.squarespace.v6.template.less.exec;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.ErrorType;
import com.squarespace.v6.template.less.ExecuteErrorType;
import com.squarespace.v6.template.less.LessCompiler;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.SyntaxErrorType;
import com.squarespace.v6.template.less.core.ErrorUtils;
import com.squarespace.v6.template.less.core.LessUtils;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;


/**
 * Runs the on-disk test suite.
 */
public class LessSuiteTest {
  
  private static final boolean VERBOSE = false;
  
  private Path suiteRootPath() {
    URL top = getClass().getClassLoader().getResource("squarespace-less-suite");
    return Paths.get(top.getPath());
  }
  
  @Test
  public void testSuite() throws IOException {
    Path rootPath = suiteRootPath();
    Path lessRoot = rootPath.resolve("less");
    Path cssRoot = rootPath.resolve("css");
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.less");
    DirectoryStream<Path> dirStream = LessUtils.getMatchingFiles(lessRoot, matcher);
    StringBuilder failures = new StringBuilder();
    for (Path lessPath : dirStream) {
      System.out.println("Executing test case 'less/" + lessPath.getFileName() + "'");

      // Read and compile the .less source
      String source = LessUtils.readFile(lessPath);
      String lessCompiled = null;
      try {
        lessCompiled = compile(source, lessRoot);
      } catch (LessException e) {
        failures.append("\nCompile error " + lessPath.getFileName() + "\n" + e.primaryError().getMessage());
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
  
  @Test
  public void testErrorSuite() throws IOException {
    Path rootPath = suiteRootPath();
    Path errorRoot = rootPath.resolve("error");
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.less");
    DirectoryStream<Path> dirStream = LessUtils.getMatchingFiles(errorRoot, matcher);
    Context ctx = new Context();
    for (Path lessPath : dirStream) {
      System.out.println("Executing test case 'error/" + lessPath.getFileName() + "'");

      String source = LessUtils.readFile(lessPath);
      List<ErrorCase> errorCases = parseErrorCases(source);
      for (ErrorCase errorCase : errorCases) {
        try {
          compile(errorCase.source, errorRoot);
          Assert.fail("Expected a LessException for error '" + errorCase.failMessage + "'");

        } catch (LessException e) {
          // Force generation of the error message, to cover that code
          String msg = ErrorUtils.formatError(ctx, lessPath, e, 4);
          if (VERBOSE) {
            System.err.println(msg);
          }
          assertEquals(errorCase.errorType, e.primaryError().type());
        }
      }      
    }
  }
  
  private String compile(String source, Path importRoot) throws LessException {
    // Setup the compiler
    Options opts = new Options();
    opts.addImportPath(importRoot.toString());
    
    Context ctx = new Context(opts);
    LessCompiler compiler = new LessCompiler();
    ctx.setCompiler(compiler);
    return compiler.compile(source, ctx);
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

  /**
   * Parse the error cases from the source, converting them into a list.
   */
  private List<ErrorCase> parseErrorCases(String source) {
    List<ErrorCase> errorCases = new ArrayList<>();
    String[] lines = source.split("\n");
    int size = lines.length;
    for (int i = 0; i < size; i++) {
      if (lines[i].startsWith("//:")) {
        String[] parts = lines[i].substring(3).split("\\s+", 2);
        if (parts.length != 2) {
          throw new RuntimeException("Bad test case definition: " + lines[i]);
        }
        
        // Read the test case's source
        ErrorType errorType = resolveErrorType(parts[0]);
        StringBuilder buf = new StringBuilder();
        i++;
        while (i < size) {
          if (lines[i].startsWith("//:")) {
            i--;
            break;
          }
          buf.append(lines[i]).append('\n');
          i++;
        }
        errorCases.add(new ErrorCase(errorType, parts[1], buf.toString()));
      }
    }
    return errorCases;
  }
  
  private ErrorType resolveErrorType(String name) {
    int index = name.indexOf('.');
    if (index == -1) {
      throw new RuntimeException("The ErrorType name must be of the form CLASS.MEMBER");
    }
    String cls = name.substring(0, index);
    String member = name.substring(index + 1);
    switch (cls) {
      case "ExecuteErrorType":
        return ExecuteErrorType.valueOf(member);
      case "SyntaxErrorType":
        return SyntaxErrorType.valueOf(member);
      default:
        throw new RuntimeException("Unknown ErrorType-derived class: " + cls);  
    }
  }
  
  private static class ErrorCase {

    private ErrorType errorType;
    
    private String failMessage;
    
    private String source;
    
    public ErrorCase(ErrorType errorType, String failMessage, String source) { 
      this.errorType = errorType;
      this.failMessage = failMessage;
      this.source = source;
    }

  }
  
}
