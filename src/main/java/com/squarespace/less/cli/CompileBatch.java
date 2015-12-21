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

package com.squarespace.less.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.cli.LessC.Args;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Stylesheet;


/**
 * Compiles a batch of files.
 */
class CompileBatch extends BaseCompile {

  CompileBatch(Args args, PrintStream out, PrintStream err) {
    super(args, out, err, System.in);
  }

  @Override
  public int process() {
    long start = System.nanoTime();

    Path input = Paths.get(args.input());
    if (!input.toFile().isDirectory()) {
      return fail("the path '" + input + "' cannot be read.\n");
    }

    Path output = input;
    if (args.output() != null) {
      output = Paths.get(args.output());
    }

    input = input.toAbsolutePath();
    output = output.toAbsolutePath();
    if (output.toFile().exists() && !output.toFile().isDirectory()) {
      log("ERROR: batch mode expected '" + output + "' to be a directory");
      return ERR;
    }

    Map<Path, Stylesheet> cache = new HashMap<>();
    boolean error = false;

    try {
      log("Parsing and caching stylesheets:\n");
      List<Path> lessPaths = LessUtils.getMatchingFiles(input, "glob:*.less", true);
      for (Path path : lessPaths) {
        try {
          path = input.resolve(path).toAbsolutePath().normalize();
          Stylesheet sheet = parse(path);
          cache.put(path, sheet);

        } catch (LessException e) {
          error = true;
        }
      }
      if (args.verbose()) {
        emitMemory("post-parse");
      }

      log("Compiling stylesheets:\n");
      Files.createDirectories(output);
      for (Path path : lessPaths) {
        path = input.resolve(path).toAbsolutePath().normalize();
        Stylesheet stylesheet = cache.get(path);
        if (stylesheet == null) {
          log("ERROR: '" + path + "' was not cached.\n");
          continue;
        }

        Path fileName = path.getFileName();
        String[] fileParts = fileName.toString().split("\\.(?=[^\\.]+$)");
        Path cssPath = output.resolve(fileParts[0] + ".css").normalize();

        log("compiling " + fileName + " to " + cssPath);
        LessContext ctx = new LessContext(args.compilerOptions(), null, cache);
        try {

          long compileStart = System.nanoTime();
          ctx.setCompiler(compiler);
          String css = compiler.render(stylesheet.copy(), ctx);
          LessUtils.writeFile(cssPath, css);
          logElapsed(" ", compileStart, System.nanoTime());

        } catch (LessException e) {
          standardErr.println("\n\n" + ErrorUtils.formatError(ctx, path, e, 4) + SEPARATOR + "\n");
          error = true;
        }
      }

    } catch (NoSuchFileException e) {
      log("ERROR: cannot locate path " + e.getMessage());
      error = true;

    } catch (IOException e) {
      log("ERROR: " + e.getMessage());
      error = true;
    }

    if (args.verbose()) {
      emitMemory("post-compiler");
    }

    if (error) {
      return ERR;
    }

    logElapsed("\nCompleted in", start, System.nanoTime());
    return OK;
  }

  /**
   * Read and parse the stylesheet.
   */
  private Stylesheet parse(Path path) throws LessException, IOException {
    LessContext ctx = new LessContext(args.compilerOptions());
    try {
      String data = LessUtils.readFile(path);
      Stylesheet result = null;
      ctx.setCompiler(compiler);
      log("parsing " + path + " ");
      long start = System.nanoTime();
      result = compiler.parse(data, ctx, path.getParent(), path.getFileName());
      double elapsed = (System.nanoTime() - start) / 1000000.0;
      standardErr.printf(" %.3fms\n", elapsed);
      return result;

    } catch (LessException e) {
      standardErr.println("\n\n" + ErrorUtils.formatError(ctx, path, e, 4) + SEPARATOR + "\n");
      return null;
    }
  }

}
