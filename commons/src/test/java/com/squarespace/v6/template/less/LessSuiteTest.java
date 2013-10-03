package com.squarespace.v6.template.less;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.parse.Parselets;


public class LessSuiteTest extends LessTestBase {

  @Test
  public void testOne() throws IOException, LessException {
    List<String> paths = Arrays.asList("comments.less", "media.less", "css.less");
    Path root = getRoot();
    for (String path : paths) {
      Path lessPath = root.resolve("less/" + path);
      LessHarness h = new LessHarness();
      String data = readFile(lessPath);
//      System.out.println(data);
//      System.out.println("--------------------------------------------------------");
      System.out.println(h.execute(data));
      System.out.println("--------------------------------------------------------");
    }
  }
  
  @Test
  public void testSqspSuite() throws IOException, LessException {
//    String strRoot = "/Users/phensley/work/less_work/sqsp-tests";
    String strRoot = "/Users/phensley/dev/squarespace-v6/scripts";
    Path root = Paths.get(strRoot);
    Path lessPath = root.resolve("test.less");
    LessHarness h = new LessHarness();
    String data = readFile(lessPath);
    Node res = h.parse(data);
    System.out.println(res);
    System.out.println(h.execute(data));
  }
  
  @Test
  public void testSuite() throws IOException, LessException { 
    String strRoot = "/Users/phensley/work/less_work/less.js/test";
//    String strRoot = "/Users/phensley/work/less_work/sqsp-tests";
    Path root = Paths.get(strRoot);
    Path lessDir = root.resolve("less");
//    Path cssDir = root.resolve("css");
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.less");
    DirectoryStream<Path> stream = getMatchingFiles(lessDir, matcher);
    for (Path path : stream) {
      String name = path.getFileName().toString();
      if (name.startsWith("import") || name.startsWith("javascript")) {
        continue;
      }
      Path lessPath = lessDir.resolve(path);
//      Path cssPath = cssDir.resolve(path);
      System.out.println(lessPath);
      Options opts = new Options();
      opts.importRoot(root.toString());

      LessHarness h = new LessHarness();
      String data = readFile(lessPath);
      System.out.println("-----------------------------");
      try {
        String res = h.execute(data, opts);
        System.out.println(res);
        
      } catch (LessException | RuntimeException e) {
        e.printStackTrace();
      }
    }
  }
  
  private Stylesheet parse(Path path) throws LessException {
    LessHarness h = new LessHarness(Parselets.STYLESHEET);
    Stylesheet result = (Stylesheet)h.parse(readFile(path));
    return result;
  }

  private Path getRoot() {
    String root = "/Users/phensley/work/less_work/less.js/test";
//    String root = "/Users/phensley/work/less_work/sqsp-tests";
    FileSystem fs = FileSystems.getDefault();
    return fs.getPath(root);
  }
 
  private static DirectoryStream<Path> getMatchingFiles(Path dir, final PathMatcher matcher) throws IOException {
    return Files.newDirectoryStream(dir, new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path entry) throws IOException {
        return !Files.isDirectory(entry) && matcher.matches(entry.getFileName());
      }
    });
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
