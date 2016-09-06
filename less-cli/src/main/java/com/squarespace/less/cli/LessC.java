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

import static com.squarespace.less.LessCompiler.LESSJS_VERSION;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.apache.commons.lang3.StringUtils;

import com.squarespace.less.LessBuildProperties;
import com.squarespace.less.LessOptions;


/**
 * Main command line interface for the compiler.
 *
 * TODO: move to separate gradle subproject
 */
public class LessC {

  private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("window");

  private static final String LESS_REPOSITORY = "http://github.com/squarespace/less-compiler";

  private static final String IMPLEMENTATION = "[Java, Squarespace]";

  private static final String PROGRAM_NAME = "lessc";

  private final PrintStream err;

  /**
   * Constructs a command-line compiler which writes output to the
   * given out and err streams.
   */
  public LessC(PrintStream err) {
    this.err = err;
  }

  /**
   * Main entry point for the command-line compiler.
   */
  public static void main(String[] rawArgs) {
    System.exit(process(rawArgs, System.out, System.err, System.in));
  }

  /**
   * Effectively this is the main() method, but separated so it can be
   * unit tested and all output captured.
   */
  public static int process(String[] rawArgs, PrintStream out, PrintStream err, InputStream in) {
    LessC cmd = new LessC(err);

    // Bit of a catch-22 here at the moment, since we need to parse the arguments
    // and report errors before knowing which implementation to invoke.
    Args args = cmd.parseArguments(rawArgs);
    if (args == null) {
      System.exit(BaseCompile.ERR);
    }

    // Select the implementation based on the parsed arguments.
    BaseCompile impl = null;
    if (args.batchMode()) {
      impl = new CompileBatch(args, out, err);
    } else {
      impl = new CompileSingle(args, out, err, in);
    }
    return impl.process();
  }

  /**
   * Constructs the parser for the command line arguments and parses
   * the arguments.  Also prints the compiler version string.
   */
  public Args parseArguments(String[] args) {
    String version = buildVersion();

    ArgumentParser parser = ArgumentParsers.newArgumentParser(PROGRAM_NAME)
      .description("Compile .less files into .css")
      .version(version)
      .setDefault("recursion_limit", LessOptions.DEFAULT_RECURSION_LIMIT)
      .setDefault("indent", LessOptions.DEFAULT_INDENT);

    parser.addArgument("--batch", "-b")
      .action(Arguments.storeTrue())
      .help("Batch mode");

    parser.addArgument("--debug")
      .type(LessDebugMode.class)
      .choices(LessDebugMode.values())
      .help("Enables debug mode.");

    parser.addArgument("--indent", "-i")
      .metavar("SPACES")
      .type(Integer.class)
      .help("Number of spaces of indent.");

    parser.addArgument("--import-once")
      .action(Arguments.storeTrue())
      .help("When enabled, stylesheets will only be imported once.");

    parser.addArgument("--include-paths", "-I")
      .metavar("PATH")
      .type(String.class)
      .help("Set include paths. Separated by ':'. Use ';' on Windows");

    parser.addArgument("--lint", "-l")
      .action(Arguments.storeTrue())
      .help("Syntax check only (lint).");

    parser.addArgument("--mixin-recursion-limit", "-r")
      .metavar("LIMIT")
      .type(Integer.class)
      .setDefault(LessOptions.DEFAULT_RECURSION_LIMIT)
      .help("Sets the recursion depth limit.");

    parser.addArgument("--import-recursion-limit", "-R")
      .metavar("LIMIT")
      .type(Integer.class)
      .setDefault(LessOptions.DEFAULT_RECURSION_LIMIT)
      .help("Sets the import recursion depth limit.");

    parser.addArgument("--statistics", "-s")
      .action(Arguments.storeTrue())
      .help("Output compile statistics");

    parser.addArgument("--strict")
      .action(Arguments.storeTrue())
      .help("Enables strict mode. Throws errors instead of warnings for some invalid rules.");

    parser.addArgument("--tracing", "-t")
      .action(Arguments.storeTrue())
      .help("Enables tracing for execution.");

    parser.addArgument("--version", "-v")
      .action(Arguments.version())
      .help("Show the version and exit");

    parser.addArgument("--verbose", "-V")
      .action(Arguments.storeTrue())
      .help("Enables verbose mode");

    parser.addArgument("--wait", "-w")
      .action(Arguments.storeTrue())
      .help("Waits for user input before executing. For profiling purposes.");

    parser.addArgument("--compress", "-x")
      .action(Arguments.storeTrue())
      .help("Enables compressing whitespace (minification)");

    parser.addArgument("input")
      .type(String.class)
      .help("Input file, or a directory in batch mode.");

    parser.addArgument("output")
      .type(String.class)
      .nargs("?")
      .help("Output file, or a directory in batch mode.");

    try {
      Namespace res = parser.parseArgs(args);

      // Options used by the compiler.
      LessOptions opts = new LessOptions();
      opts.compress(res.getBoolean("compress"));
      opts.importOnce(res.getBoolean("import_once"));
      opts.importPaths(parseImportPaths(res));
      opts.indent(res.getInt("indent"));
      opts.mixinRecursionLimit(res.getInt("mixin_recursion_limit"));
      opts.importRecursionLimit(res.getInt("import_recursion_limit"));
      opts.strict(res.getBoolean("strict"));
      opts.tracing(res.getBoolean("tracing"));
      opts.hideWarnings(false);

      // Options used by the command.
      Args cmdArgs = new Args();
      cmdArgs.programName = PROGRAM_NAME;
      cmdArgs.input = res.getString("input");
      cmdArgs.output = res.getString("output");
      cmdArgs.batchMode = res.getBoolean("batch");
      cmdArgs.compilerOptions = opts;
      cmdArgs.debugMode = res.<LessDebugMode>get("debug");
      cmdArgs.lintOnly = res.getBoolean("lint");
      cmdArgs.statistics = res.getBoolean("statistics");
      cmdArgs.verbose = res.getBoolean("verbose");
      cmdArgs.waitForUser = res.getBoolean("wait");

      if (cmdArgs.verbose() && cmdArgs.debugMode() != null) {
        dumpArguments(res);
      }

      return cmdArgs;

    } catch (ArgumentParserException e) {
      parser.handleError(e);
      return null;
    }
  }

  private List<String> parseImportPaths(Namespace res) {
    String paths = res.getString("include_paths");
    if (paths == null) {
      return null;
    }
    char sep = IS_WINDOWS ? ';' : ':';
    String[] parts = StringUtils.split(paths, sep);
    return Arrays.asList(parts);
  }

  private void dumpArguments(Namespace ns) {
    err.println("Command line arguments:");
    Map<String, Object> attrs = ns.getAttrs();
    for (String key : attrs.keySet()) {
      err.printf(" %16s: %s\n", key, attrs.get(key));
    }
  }

  /**
   * Build the version string.
   */
  private String buildVersion() {
    StringBuilder buf = new StringBuilder();
    buf.append("${prog} version ")
      .append(LessBuildProperties.version())
      .append(' ')
      .append(IMPLEMENTATION)
      .append('\n');
    buf.append("      repository: ").append(LESS_REPOSITORY).append('\n');
    buf.append("   compatibility: ").append(LESSJS_VERSION).append('\n');
    buf.append("      build date: ").append(LessBuildProperties.date()).append('\n');
    buf.append("    build commit: ").append(LessBuildProperties.commit()).append('\n');
    return buf.toString();
  }

  /**
   * Command line and compiler arguments.
   */
  public static class Args {

    private String programName;

    private String input;

    private String output;

    private boolean batchMode;

    private LessOptions compilerOptions;

    private LessDebugMode debugMode;

    private boolean lintOnly;

    private boolean statistics;

    private boolean verbose;

    private boolean waitForUser;


    private Args() {
    }

    public String programName() {
      return programName;
    }

    public String input() {
      return input;
    }

    public String output() {
      return output;
    }

    public boolean batchMode() {
      return batchMode;
    }

    public LessOptions compilerOptions() {
      return compilerOptions;
    }

    public LessDebugMode debugMode() {
      return debugMode;
    }

    public boolean lintOnly() {
      return lintOnly;
    }

    public boolean statsEnabled() {
      return statistics;
    }

    public boolean verbose() {
      return verbose;
    }

    public boolean waitForUser() {
      return waitForUser;
    }

  }

}
