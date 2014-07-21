/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less;

import static com.squarespace.less.LessCompiler.LESSJS_VERSION;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Stylesheet;


/**
 * Batch-compile Squarespace LESS rollups. Reads all files from disk,
 * parses them and caches the parsed trees in a hashmap. Once all files
 * are parsed, it proceeds to compile them, only writing .css files
 * for which no errors were thrown.
 *
 * TODO:  merge this in as a subcommand of LessC ("batch" mode).
 */
public class LessBatchC extends LessBaseCommand {

  private static final String PROGRAM_NAME = "sqs_batch_lessc";

  private static final String IMPLNAME = "(LESS Batch Compiler) [Java, Squarespace]";

  private static final LessCompiler COMPILER = new LessCompiler();

  private final LessOptions options = new LessOptions();

  @Parameter(description = "LESS_DIR [OUTPUT_DIR]")
  private List<String> args;

  @Parameter(names = { "-C", "-compile" }, variableArity = true, description = "Only write final CSS for the "
      + "listed files. The rest of the files in the LESS_DIR will be parsed and treated as includes.")
  private List<String> compileOnly;

  @Parameter(names = { "-I", "-import-path" }, description = "Add path to the list of import paths")
  public List<String> importPaths;

  @Parameter(names = { "-T", "-tracing" }, description = "Trace execution")
  private boolean tracing = false;

  @Parameter(names = { "-V", "-verbose" }, description = "Verbose")
  private boolean verbose = false;

  @Parameter(names = { "-h", "-help" }, description = "Show usage", help = true)
  private boolean help;

  @Parameter(names = { "-i", "-indent" }, description = "Indent size")
  private int indent = 2;

  @Parameter(names = { "-import-once" }, description = "Imports are only processed once")
  private boolean importOnce;

  @Parameter(names = { "-strict" }, description = "Strict mode, throws exceptions when some invalid rules are "
      + "evaluated instead of embedding warnings.")
  private boolean strictMode;

  @Parameter(names = { "-x", "-compress" }, description = "Compress mode")
  private boolean compress;

  @Parameter(names = { "-v", "-version" }, description = "Show version")
  private boolean version = false;

  private void buildOptions() {
    options.compress(compress);
    options.importOnce(importOnce);
    options.indent(indent);
    options.tracing(tracing);
    options.strict(strictMode);
    options.hideWarnings(false);
    if (importPaths != null) {
      options.importPaths(importPaths);
    }
  }

  public static String version() {
    return PROGRAM_NAME + " compatibility=" + LESSJS_VERSION + "  " + IMPLNAME;
  }

  @Override
  public String programName() {
    return PROGRAM_NAME;
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

  private void processAll(Path inputPath, Path outputPath) {
    List<Path> lessPaths = null;
    Map<Path, Stylesheet> preCache = new HashMap<>();
    boolean error = false;
    try {
      log("beginning parse and pre-cache:\n");
      lessPaths = LessUtils.getMatchingFiles(inputPath, GLOB_LESS, true);
      for (Path path : lessPaths) {
        Path realPath = inputPath.resolve(path).toAbsolutePath().normalize();
        String data = LessUtils.readFile(realPath);
        Stylesheet stylesheet = null;
        LessContext ctx = new LessContext(options);
        ctx.setCompiler(COMPILER);
        try {
          log("parsing " + path + " ");
          long start = System.nanoTime();
          stylesheet = COMPILER.parse(data, ctx, realPath.getParent(), realPath.getFileName());
          double elapsed = (System.nanoTime() - start) / 1000000.0;
          System.err.printf("  %.3fms\n", elapsed);
          preCache.put(realPath, stylesheet);

        } catch (LessException e) {
          System.err.println(ErrorUtils.formatError(ctx, path, e, 4) + "\n");
          error = true;
        }
      }

      // If the compile filter has been set, only compile the files mentioned in the list.
      if (compileOnly != null && !compileOnly.isEmpty()) {
        lessPaths.clear();
        for (String name : compileOnly) {
          lessPaths.add(Paths.get(name));
        }
      }

      if (verbose) {
        emitMemory("post-parse");
      }

      log("beginning compile:\n");
      Files.createDirectories(outputPath);
      for (Path path : lessPaths) {
        Path realPath = inputPath.resolve(path).toAbsolutePath().normalize();
        Stylesheet stylesheet = preCache.get(realPath);
        if (stylesheet == null) {
          log("error: '" + path.toString() + "' was not pre-cached. exiting.\n");
          System.exit(1);
        }

        LessContext ctx = new LessContext(options, null, preCache);
        try {
          String[] fileParts = path.getFileName().toString().split("\\.(?=[^\\.]+$)");
          Path parentPath = path.getParent();
          if (parentPath != null) {
            // We have an intermediate subdir in the output path. Create it.
            realPath = outputPath.resolve(parentPath);
            Files.createDirectories(realPath);
          } else {
            realPath = outputPath;
          }

          Path cssPath = realPath.resolve(fileParts[0] + ".css").normalize();
          log("compiling to " + cssPath);
          long start = System.nanoTime();
          ctx.setCompiler(COMPILER);
          String cssData = COMPILER.render(stylesheet.copy(), ctx);
          writeFile(cssPath, cssData);
          double elapsed = (System.nanoTime() - start) / 1000000.0;
          System.err.printf("  %.3fms\n", elapsed);

        } catch (LessException e) {
          System.err.println("\n\n" + ErrorUtils.formatError(ctx, path, e, 4) + SEPARATOR + "\n");
          error = true;
        }
      }

    } catch (NoSuchFileException e) {
      log("ERROR: cannot locate path " + e.getMessage());
      error = true;

    } catch (IOException ioe) {
      log("ERROR: " + ioe.getMessage());
      error = true;
    }

    if (verbose) {
      emitMemory("post-compile");
    }

    if (error) {
      System.exit(1);
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
