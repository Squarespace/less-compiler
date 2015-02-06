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
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ErrorUtils;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.Stylesheet;


/**
 * Command-line implementation.
 */
public class LessCImpl extends LessBaseCommand {

  private static final String PROGRAM_NAME = "sqs_lessc";

  private static final String IMPLNAME = "(LESS Compiler) [Java, Squarespace]";

  private static final int OK = 0;

  private static final int ERROR = 1;

  private final LessOptions options = new LessOptions();

  private final PrintStream out;

  private final PrintStream err;

  @Parameter(description = "LESS_FILE [OUTPUT_FILE]")
  private final List<String> args = new ArrayList<String>();

  // TODO: emit DebugMode.description() in usage
  @Parameter(
      names = { "-D" },
      description = "Debug mode (canonical, parse, expand)",
      converter = DebugModeConverter.class)
  private LessDebugMode debugMode;

// TODO: low priority
//  @Parameter(names = { "-L", "-lines" }, description = "Line number")
//  private boolean lineNumbers;

  @Parameter(names = { "-I", "-import-path" }, description = "Add path to the list of import paths")
  public List<String> importPaths;

  @Parameter(names = { "-R" }, description = "Recursion limit")
  private int recursionLimit = LessOptions.DEFAULT_RECURSION_LIMIT;

  @Parameter(names = { "-S", "-stats" }, description = "Output statistics")
  private boolean stats = false;

  @Parameter(names = { "-T", "-tracing" }, description = "Trace execution")
  private boolean tracing = false;

  @Parameter(names = { "-V", "-verbose" }, description = "Verbose")
  private boolean verbose = false;

  @Parameter(names = { "-W", "-wait" }, description = "Wait before executing / exiting.")
  private boolean waitForUser = false;

  @Parameter(names = { "-h", "-help" }, description = "Show usage", help = true)
  private boolean help;

  @Parameter(names = { "-i", "-indent" }, description = "Indent size")
  private int indent = LessOptions.DEFAULT_INDENT;

  @Parameter(names = { "-import-once" }, description = "Imports are only processed once")
  private boolean importOnce;

  @Parameter(names = { "-strict" }, description = "Strict mode, throws exceptions when some invalid rules are "
      + "evaluated instead of embedding warnings.")
  private boolean strictMode;

  @Parameter(names = { "-v", "-version" }, description = "Show version")
  private boolean version = false;

  @Parameter(names = { "-x", "-compress" }, description = "Compress mode")
  private boolean compress = false;

  public LessCImpl(PrintStream out, PrintStream err) {
    this.out = out;
    this.err = err;
  }

  private void buildOptions() {
    options.compress(compress);
    options.importOnce(importOnce);
    options.indent(indent);
// TODO:
//    options.lineNumbers(lineNumbers);
    options.recursionLimit(recursionLimit);
    options.strict(strictMode);
    options.hideWarnings(false);
    options.tracing(tracing);
    if (importPaths != null) {
      options.importPaths(importPaths);
    }
  }

  public String version() {
    StringBuilder buf = new StringBuilder();
    buf.append(PROGRAM_NAME)
       .append(" compat level ")
       .append(LESSJS_VERSION)
       .append("  ")
       .append(IMPLNAME);
    if (verbose) {
      buf.append("\n  version: ").append(LessBuildProperties.version())
         .append("\n     date: ").append(LessBuildProperties.date())
         .append("\n   commit: ").append(LessBuildProperties.commit());
    }
    return buf.toString();
  }

  @Override
  public String programName() {
    return PROGRAM_NAME;
  }

  public boolean initialize(String[] args) {
    JCommander cmd = new JCommander(this);
    cmd.setProgramName(PROGRAM_NAME);
    try {
      cmd.parse(args);
    } catch (ParameterException e) {
      err.println(e.getMessage());
      return false;
    }
    if (help) {
      cmd.usage();
      return false;
    }
    if (version) {
      out.println(version());
      return false;
    }
    buildOptions();
    return true;
  }

  public boolean waitForUser() {
    return waitForUser;
  }

  public int run() {
    if (args == null || args.isEmpty()) {
      return fail("you must provide a .less file.");
    }
    Path path = Paths.get(args.get(0));
    if (!path.toFile().isFile()) {
      return fail("the path '" + path + "' cannot be read.");
    }
    Path rootPath = path.toAbsolutePath().getParent();
    Path fileName = path.getFileName();
    LessCompiler compiler = new LessCompiler();
    String source = null;
    try {
      source = LessUtils.readFile(path);
    } catch (IOException e) {
      return fail("error reading '" + path + "': " + e.getMessage());
    }

    LessContext ctx = new LessContext(options);
    ctx.setCompiler(compiler);
    try {
      if (debugMode == LessDebugMode.CANONICAL) {
        Stylesheet stylesheet = compiler.parse(source, ctx, rootPath, fileName);
        out.println(canonicalize(stylesheet));

      } else if (debugMode == LessDebugMode.PARSE) {
        Stylesheet stylesheet = compiler.parse(source, ctx, rootPath, fileName);
        out.println(parseTree(stylesheet));

      } else if (debugMode == LessDebugMode.EXPAND) {
        // NOTE: This mode doesn't fully work yet.
        Stylesheet stylesheet = compiler.parse(source, ctx, rootPath, fileName);
        stylesheet = compiler.expand(stylesheet, ctx);
        out.println(canonicalize(stylesheet));

      } else {
        String result = compiler.compile(source, ctx, rootPath, fileName);
        if (args.size() >= 2) {
          try {
            LessUtils.writeFile(Paths.get(args.get(1)), result);
          } catch (IOException e) {
            err.println("\n    " + e.getMessage());
          }
        } else {
          out.print(result);
        }
      }

    } catch (LessException e) {
      err.println(ErrorUtils.formatError(ctx, path, e, 4));
      return ERROR;
    }
    if (stats) {
      emitStats(ctx.stats());
    }
    if (verbose) {
      emitMemory("post-compile");
    }

    // In trace mode, check for buffer leaks.
    if (options.tracing()) {
      ctx.sanityCheck();
    }
    return OK;
  }

  private int fail(String message) {
    err.println(message);
    return ERROR;
  }

  private String canonicalize(Stylesheet stylesheet) {
    Buffer buf = new Buffer(options.indent());
    stylesheet.repr(buf);
    return buf.toString();
  }

  private String parseTree(Stylesheet stylesheet) {
    Buffer buf = new Buffer(options.indent());
    stylesheet.modelRepr(buf);
    return buf.toString();
  }

  private String emitStats(LessStats stats) {
    StringBuilder buf = new StringBuilder();
    buf.append("--------------------------------------------------------\n");
    buf.append(PROGRAM_NAME).append(" statistics:\n");
    buf.append("    parse time: ").append(stats.parseTimeMs()).append("ms\n");
    buf.append("  compile time: ").append(stats.compileTimeMs()).append("ms\n");
    buf.append("disk wait time: ").append(stats.diskWaitTimeMs()).append("ms\n");
    buf.append("  import count: ").append(stats.importCount()).append('\n');
    return buf.toString();
  }

  public static class DebugModeConverter implements IStringConverter<LessDebugMode> {
    @Override
    public LessDebugMode convert(String value) {
      try {
        return LessDebugMode.fromString(value);

      } catch (IllegalArgumentException e) {
        throw new ParameterException("Unknown debug mode '" + value + "'. "
            + "Available modes are: " + LessDebugMode.modes());
      }
    }
  }

}
