package com.squarespace.v6.template.less;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.model.Stylesheet;


/**
 * Batch-compile Squarespace LESS rollups. Reads all files from disk,
 * parses them and caches the parsed trees in a hashmap. Once all files
 * are parsed, it proceeds to compile them, only writing .css files 
 * for which no errors were thrown.
 */
public class LessBatchC {

  private static final String PROGRAM_NAME = "sqs_batch_lessc";
  
  private static final String IMPLNAME = "(LESS Batch Compiler) [Java, Squarespace]";
  
  private static final String VERSION = "1.3.3";
  
  @Parameter
  private List<String> args;

  @Parameter(names = { "-h", "-help" }, description = "Show usage", help = true)
  private boolean help;
  
  @Parameter(names = { "-i", "-indent" }, description = "Indent size")
  private int indent = 2;
  
  @Parameter(names = { "-x", "-compress" }, description = "Compress mode")
  private boolean compress;
  
  @Parameter(names = { "-v", "-version" }, description = "Show version")
  private boolean version = false;

  private Options options = new Options();
  
  private void buildOptions() {
    options.compress(compress);
    options.indent(indent);
  }
  
  public static String version() {
    return PROGRAM_NAME + " " + VERSION + " " + IMPLNAME;
  }
  
  public static void main(String[] args) {
    LessBatchC rollups = new LessBatchC();
    JCommander cmd = new JCommander(rollups);
    cmd.setProgramName(PROGRAM_NAME);
    try {
      cmd.parse(args);
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    if (rollups.help) { 
      cmd.usage();
      System.exit(0);
    }
    long start = System.nanoTime();
    rollups.main();
    double elapsed = (System.nanoTime() - start) / 1000000.0;
    System.err.printf("\nCompleted in %.3fms\n", elapsed);
  }

  private void main() {
    if (version) {
      System.out.println(version());
      System.exit(0);
    }
    buildOptions();
    if (args.isEmpty()) {
      System.err.println("you must provide the path containing the .less files");
      System.exit(1);
    }
    Path parentPath = Paths.get(args.get(0));
    processAll(parentPath);
  }
  
  private void processAll(Path rootPath) {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:*.less");
    Map<Path, Stylesheet> cache = new HashMap<>();
    try {
      System.err.println("\nPARSING AND CACHING ..\n");
      Context ctx = new Context(options);
      LessCompiler compiler = new LessCompiler();
      DirectoryStream<Path> dirStream = LessUtils.getMatchingFiles(rootPath, matcher);
      for (Path path : dirStream) {
        String data = LessUtils.readFile(path);
        Stylesheet stylesheet = null;
        try {
          System.err.print("Parsing " + path + " ");
          long start = System.nanoTime();
          stylesheet = (Stylesheet) compiler.parse(data, ctx, path.getParent());
          double elapsed = (System.nanoTime() - start) / 1000000.0;
          System.err.printf("  %.3fms\n", elapsed);
          cache.put(path, stylesheet);
          
        } catch (LessException e) {
          System.err.println(e.errorInfo().getMessage());
        }
      }

      System.err.println("\nCOMPILING ..\n");
      dirStream = LessUtils.getMatchingFiles(rootPath, matcher);
      ctx = new Context(options, null, cache);
      ctx.setCompiler(compiler);
      for (Path path : dirStream) {
        Stylesheet stylesheet = cache.get(path);
        if (stylesheet == null) {
          continue;
        }
        try {
          String[] parts = path.toString().split("\\.(?=[^\\.]+$)");
          Path cssPath = Paths.get(parts[0] + ".css").normalize();
          System.err.print("Compiling to " + cssPath + " ");
          long start = System.nanoTime();
          String cssData = compiler.render(stylesheet.copy(), ctx);
          writeFile(cssPath, cssData);
          double elapsed = (System.nanoTime() - start) / 1000000.0;
          System.err.printf("  %.3fms\n", elapsed);
          
        } catch (LessException e) {
          System.err.println("\n    " + e.errorInfo().getMessage());
        }
      }
      
    } catch (IOException ioe) {
      System.out.println(ioe.getMessage());
    }
  }
  
  private static void writeFile(Path outPath, String data) {
    try (OutputStream output = Files.newOutputStream(outPath, CREATE, TRUNCATE_EXISTING)) {
      IOUtils.write(data, output);
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("\n    " + e.getMessage());
    }
  }
  
}
