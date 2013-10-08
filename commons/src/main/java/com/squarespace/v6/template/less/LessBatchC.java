package com.squarespace.v6.template.less;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.squarespace.v6.template.less.core.ErrorUtils;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.model.Stylesheet;


/**
 * Batch-compile Squarespace LESS rollups. Reads all files from disk,
 * parses them and caches the parsed trees in a hashmap. Once all files
 * are parsed, it proceeds to compile them, only writing .css files 
 * for which no errors were thrown.
 * 
 * TODO:  merge this in as a subcommand of LessC ("batch" mode).
 */
public class LessBatchC extends BaseCommand {

  private static final String PROGRAM_NAME = "sqs_batch_lessc";
  
  private static final String IMPLNAME = "(LESS Batch Compiler) [Java, Squarespace]";
  
  @Parameter(description = "LESS_DIR [OUTPUT_DIR]")
  private List<String> args;

  @Parameter(names = { "-C", "-compile" }, variableArity = true, description = "Only write final CSS for the "
      + "listed files. The rest of the files in the LESS_DIR will be parsed and treated as includes.")
  private List<String> compileOnly;
  
  @Parameter(names = { "-T", "-tracing" }, description = "Trace execution")
  private boolean tracing = false;

  @Parameter(names = { "-h", "-help" }, description = "Show usage", help = true)
  private boolean help;
  
  @Parameter(names = { "-i", "-indent" }, description = "Indent size")
  private int indent = 2;
  
  @Parameter(names = { "-import-once" }, description = "Imports are only processed once")
  private boolean importOnce;
  
  @Parameter(names = { "-x", "-compress" }, description = "Compress mode")
  private boolean compress;
  
  @Parameter(names = { "-v", "-version" }, description = "Show version")
  private boolean version = false;

  private Options options = new Options();
  
  private void buildOptions() {
    options.compress(compress);
    options.importOnce(importOnce);
    options.indent(indent);
    options.tracing(tracing);
  }
  
  public static String version() {
    return PROGRAM_NAME + " " + VERSION + " " + IMPLNAME;
  }
  
  public static void main(String[] args) {
    LessBatchC batcher = new LessBatchC();
    JCommander cmd = new JCommander(batcher);
    cmd.setProgramName(PROGRAM_NAME);
    try {
      cmd.parse(args);
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    if (batcher.help) { 
      cmd.usage();
      System.exit(0);
    }
    long start = System.nanoTime();
    batcher.main();
    double elapsed = (System.nanoTime() - start) / 1000000.0;
    System.err.printf("\nCompleted in %.3fms\n", elapsed);
  }

  private void main() {
    if (version) {
      System.out.println(version());
      System.exit(0);
    }
    buildOptions();
    if (args == null || args.isEmpty()) {
      System.err.println("you must provide the directory containing the .less files");
      System.exit(1);
    }
    Path inputPath = Paths.get(args.get(0));
    Path outputPath = inputPath;
    if (args.size() >= 2) {
      outputPath = Paths.get(args.get(1));
    }
    processAll(inputPath, outputPath);
  }
  
  private PrintStream log(String msg) {
    System.err.print(PROGRAM_NAME);
    System.err.print(": ");
    System.err.print(msg);
    return System.err;
  }
  
  private void processAll(Path inputPath, Path outputPath) {
    List<Path> lessPaths = null;
    Map<Path, Stylesheet> preCache = new HashMap<>();
    try {
      log("beginning parse and pre-cache:\n");
      Context ctx = new Context(options);
      LessCompiler compiler = new LessCompiler();
      lessPaths = getMatchingFiles(inputPath, GLOB_LESS, true);
      for (Path path : lessPaths) {
        String data = LessUtils.readFile(path);
        Stylesheet stylesheet = null;
        try {
          log("parsing " + path + " ");
          long start = System.nanoTime();
          stylesheet = (Stylesheet) compiler.parse(data, ctx, path.getParent(), path.getFileName());
          double elapsed = (System.nanoTime() - start) / 1000000.0;
          System.err.printf("  %.3fms\n", elapsed);
          preCache.put(path, stylesheet);
          
        } catch (LessException e) {
          System.err.println(ErrorUtils.formatError(ctx, path, e, 4) + "\n");
        }
      }

      // If the compile filter has been set, only compile the files mentioned in the list.
      if (compileOnly != null && !compileOnly.isEmpty()) {
        lessPaths.clear();
        for (String name : compileOnly) {
          lessPaths.add(inputPath.resolve(name));
        }
      }

      log("beginning compile:\n");
      for (Path path : lessPaths) {
        Stylesheet stylesheet = preCache.get(path);
        if (stylesheet == null) {
          log("error: '" + path.toString() + "' was not pre-cached. exiting.\n");
          System.exit(1);
        }

        try {
          String[] fileParts = path.getFileName().toString().split("\\.(?=[^\\.]+$)");
          Path cssPath = outputPath.resolve(fileParts[0] + ".css").normalize();
          log("compiling to " + cssPath);
          long start = System.nanoTime();
          ctx = new Context(options, null, preCache);
          ctx.setCompiler(compiler);
          String cssData = compiler.render(stylesheet.copy(), ctx);
          writeFile(cssPath, cssData);
          double elapsed = (System.nanoTime() - start) / 1000000.0;
          System.err.printf("  %.3fms\n", elapsed);
          
        } catch (LessException e) {
          System.err.println("\n\n" + ErrorUtils.formatError(ctx, path, e, 4) + SEPARATOR + "\n");
        }
      }
      
    } catch (NoSuchFileException e) {
      log("cannot locate path " + e.getMessage());

    } catch (IOException ioe) {
      log(ioe.getMessage());
    }
  }
  
  private static void writeFile(Path outPath, String data) {
    try {
      LessUtils.writeFile(outPath, data);
    } catch (IOException e) {
      System.err.println("\n    " + e.getMessage());
    }
  }
  
}
