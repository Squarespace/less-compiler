package com.squarespace.v6.template.less;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.model.Stylesheet;


public class LessC {

  private static final String IMPLNAME = "(LESS Compiler) [Java, Squarespace]";
  
  private static final String VERSION = "1.3.3";
  
  @Parameter
  private List<String> args = new ArrayList<String>();

// TODO: debug mode
//  @Parameter(names = "-debug", description = "Debug mode")
//  public boolean debug = false;
  
  @Parameter(names = { "-h", "--help" }, description = "Show usage", help = true)
  private boolean help;

  @Parameter(names = { "-i", "-indent" }, description = "Indent size")
  public int indent = 2;

  @Parameter(names = { "-x", "-compress" }, description = "Compress mode" )
  public boolean compress = false;
  
  @Parameter(names = { "-v", "-version" }, description = "Show version")
  public boolean version = false;

  @Parameter(names = {"-C", "-canonicalize" }, description = "Output canonical representation of parse tree")
  public boolean canonicalize = false;

  @Parameter(names = {"-P", "-parse" }, description = "Parse only and dump parse tree")
  public boolean parseOnly = false;

  // TODO: future mode where we expand all mixins, imports and variables and then output the pre-render
  // representation of the tree.
  @Parameter(names = {"-E", "-expand" }, description = "Expand and dump the expanded less representation")
  public boolean expandOnly = false;
  
  @Parameter(names = {"-W", "-wait" }, description = "Wait before executing / exiting.")
  public boolean waitForUser = false;
  
  @Parameter(names = "-include-path", description = "Include path" )
  public String includePath;

  private Options options = new Options();
  
  private void buildOptions() {
    options.compress(compress);
//    options.debug(debug);
    options.indent(indent);
  }
  
  public static void main(String[] args) {
    LessC lessc = new LessC();
    JCommander cmd = new JCommander(lessc);
    cmd.parse(args);
    if (lessc.help) { 
      cmd.usage();
    }
    lessc.main();
  }
  
  private void main() {
    if (version) {
      System.out.println("lessc " + VERSION + " " + IMPLNAME);
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
   * Wait for a newline before executing / exiting.
   */
  private void waitForUser() {
    BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
    try {
      buffer.readLine();
    } catch (IOException e) {
      System.exit(1);
    }
  }
  
  private void execute() {
    if (args.isEmpty()) {
      System.err.println("you must provide a .less file.");
      System.exit(1);
    }
    String source = args.get(0);
    LessCompiler compiler = new LessCompiler();
    String data = compiler.readFile(Paths.get(source));
    Context ctx = new Context(options);
    ctx.setCompiler(compiler);
    try {
      if (canonicalize) {
        Stylesheet stylesheet = (Stylesheet) compiler.parse(data, ctx);
        System.out.println(canonicalize(stylesheet));
      
      } else if (parseOnly) {
        Stylesheet stylesheet = (Stylesheet) compiler.parse(data, ctx);
        System.out.println(parseTree(stylesheet));
      
      } else if (expandOnly) {
        Stylesheet stylesheet = (Stylesheet) compiler.parse(data, ctx);
        stylesheet = compiler.expand(stylesheet, ctx);
        System.out.println(canonicalize(stylesheet));

      } else {
        String result = compiler.compile(data, ctx);
        System.out.print(result);
      }
      
    } catch (LessException e) {
      System.out.println(e.errorInfo().getMessage());
      System.exit(1);
    }
  }

  private String canonicalize(Stylesheet stylesheet) {
    Buffer buf = new Buffer(4);
    stylesheet.repr(buf);
    return buf.toString();
  }

  private String parseTree(Stylesheet stylesheet) {
    Buffer buf = new Buffer(4);
    stylesheet.modelRepr(buf);
    return buf.toString();
  }

}
