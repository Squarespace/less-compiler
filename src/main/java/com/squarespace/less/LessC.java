package com.squarespace.less;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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


public class LessC extends BaseCommand {

  private static final String PROGRAM_NAME = "sqs_lessc";

  private static final String IMPLNAME = "(LESS Compiler) [Java, Squarespace]";

  private static final String VERSION = "1.3.3";

  private final Options options = new Options();

  @Parameter(description = "LESS_FILE [OUTPUT_FILE]")
  private List<String> args = new ArrayList<String>();

  // TODO: emit DebugMode.description() in usage
  @Parameter(names = { "-D" }, description = "Debug mode (canonical, parse, expand)", converter = DebugModeConverter.class)
  private DebugMode debugMode;

// TODO: low priority
//  @Parameter(names = { "-L", "-lines" }, description = "Line number")
//  private boolean lineNumbers;

  @Parameter(names = { "-I", "-import-path" }, description = "Add path to the list of import paths" )
  public List<String> importPaths;

  @Parameter(names = { "-R" }, description = "Recursion limit")
  private int recursionLimit = Options.DEFAULT_RECURSION_LIMIT;

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
  private int indent = Options.DEFAULT_INDENT;

  @Parameter(names = { "-import-once" }, description = "Imports are only processed once")
  private boolean importOnce;

  @Parameter(names = { "-strict" }, description = "Strict mode, throws exceptions when some invalid rules are "
      + "evaluated instead of embedding warnings.")
  private boolean strictMode;

  @Parameter(names = { "-v", "-version" }, description = "Show version")
  private boolean version = false;

  @Parameter(names = { "-x", "-compress" }, description = "Compress mode" )
  private boolean compress = false;

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

  public static String version() {
    return PROGRAM_NAME + " " + VERSION + " " + IMPLNAME;
  }

  @Override
  public String programName() {
    return PROGRAM_NAME;
  }

  public static void main(String[] args) {
    LessC lessc = new LessC();
    JCommander cmd = new JCommander(lessc);
    cmd.setProgramName(PROGRAM_NAME);
    try {
      cmd.parse(args);
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    if (lessc.help) {
      cmd.usage();
      System.exit(0);
    }
    lessc.main();
  }

  private void main() {
    if (version) {
      System.out.println(version());
      System.exit(0);
    }
    buildOptions();
    if (waitForUser) {
      waitForUser();
    }
    execute();
    if (waitForUser) {
      waitForUser();
    }
    System.exit(0);
  }

  /**
   * Wait for a newline at the prompt before executing / exiting. Assists with
   * debugging / profiling at the command line.
   */
  private void waitForUser() {
    BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
    try {
      buffer.readLine();
    } catch (IOException e) {
      System.exit(1);
    }
  }

  private void fail(String message) {
    System.err.println(message);
    System.exit(1);
  }

  private void execute() {
    if (args == null || args.isEmpty()) {
      fail("you must provide a .less file.");
    }
    Path path = Paths.get(args.get(0));
    if (!path.toFile().isFile()) {
      fail("the path '" + path + "' cannot be read.");
    }
    Path rootPath = path.toAbsolutePath().getParent();
    Path fileName = path.getFileName();
    LessCompiler compiler = new LessCompiler();
    String source = null;
    try {
      source = LessUtils.readFile(path);
    } catch (IOException e) {
      fail("error reading '" + path + "': " + e.getMessage());
    }

    Context ctx = new Context(options);
    ctx.setCompiler(compiler);
    try {
      if (debugMode == DebugMode.CANONICAL) {
        Stylesheet stylesheet = (Stylesheet) compiler.parse(source, ctx, rootPath, fileName);
        System.out.println(canonicalize(stylesheet));

      } else if (debugMode == DebugMode.PARSE) {
        Stylesheet stylesheet = (Stylesheet) compiler.parse(source, ctx, rootPath, fileName);
        System.out.println(parseTree(stylesheet));

      } else if (debugMode == DebugMode.EXPAND) {
        // NOTE: This mode doesn't fully work yet.
        Stylesheet stylesheet = (Stylesheet) compiler.parse(source, ctx, rootPath, fileName);
        stylesheet = compiler.expand(stylesheet, ctx);
        System.out.println(canonicalize(stylesheet));

      } else {
        String result = compiler.compile(source, ctx, rootPath, fileName);
        if (args.size() >= 2) {
          try {
            LessUtils.writeFile(Paths.get(args.get(1)), result);
          } catch (IOException e) {
            System.err.println("\n    " + e.getMessage());
          }
        } else {
          System.out.print(result);
        }
      }

    } catch (LessException e) {
      System.err.println(ErrorUtils.formatError(ctx, path, e, 4));
      System.exit(1);
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

  public static class DebugModeConverter implements IStringConverter<DebugMode> {
    @Override
    public DebugMode convert(String value) {
      try {
        return DebugMode.fromString(value);

      } catch (IllegalArgumentException e) {
        throw new ParameterException("Unknown debug mode '" + value + "'. "
            + "Available modes are: " + DebugMode.modes());
      }
    }
  }

}
