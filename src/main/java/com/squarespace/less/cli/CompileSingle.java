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
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.cli.LessC.Args;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessSyntax;


/**
 * Compiles a single file.
 */
class CompileSingle extends BaseCompile {

  CompileSingle(Args args, PrintStream out, PrintStream err, InputStream in) {
    super(args, out, err, in);
  }

  @Override
  public int process() {
    String argsInput = args.input();
    String source = null;
    Path input = null;

    try {
      if (argsInput.equals("-")) {
        // Load source from standard input
        source = LessUtils.readStream(standardIn);

      } else {
        // Load source from a file
        input = Paths.get(argsInput);
        if (!input.toFile().isFile()) {
          return fail("the path '" + input + "' cannot be read.");
        }
        input = input.toAbsolutePath();
        source = LessUtils.readFile(input);
      }
    } catch (IOException e) {
      String streamName = (input == null) ? "<stdin>" : input.toString();
      return fail("error reading '" + streamName + "': " + e.getMessage());
    }

    int code = OK;
    LessContext ctx = new LessContext(args.compilerOptions());
    ctx.setCompiler(compiler);
    try {
      if (args.debugMode() == null) {
        // Normal compile
        String result = null;
        if (input == null) {
          result = compiler.compile(source, ctx);
        } else {
          result = compiler.compile(source, ctx, input.getParent(), input.getFileName(), true);
        }
        String output = args.output();
        if (output != null) {
          try {
            LessUtils.writeFile(Paths.get(output), result);
          } catch (IOException e) {
            return fail(e.getMessage());
          }
        } else {
          standardOut.print(result);
        }

      } else {
        // Execute one of the debug modes
        LessParser parser = new LessParser(ctx, source);
        Stylesheet stylesheet = (Stylesheet) parser.parse(LessSyntax.STYLESHEET);
        switch (args.debugMode()) {

          case CANONICALIZE:
            standardOut.println(canonicalize(stylesheet));
            break;

          case EVAL:
            stylesheet = compiler.expand(stylesheet, ctx);
            standardOut.println(canonicalize(stylesheet));
            break;

          case EVALTREE:
            stylesheet = compiler.expand(stylesheet, ctx);
            standardOut.println(syntaxTree(stylesheet));
            break;

          case JSONAST:
            standardOut.println(jsonAst(stylesheet));
            break;

          case JSONREPR:
            standardOut.println(jsonRepr(stylesheet));
            break;

          case PARSETREE:
            standardOut.println(syntaxTree(stylesheet));
            break;

          default:
        }
      }

    } catch (LessException e) {
      standardErr.println("\n\n" + ErrorUtils.formatError(ctx, input, e, 4) + "\n");
      code = ERR;
    }

    if (args.statsEnabled()) {
      emitStats(ctx.stats());
    }

    if (args.verbose()) {
      emitMemory("post-compile");
    }

    ctx.sanityCheck();
    return code;
  }

}
