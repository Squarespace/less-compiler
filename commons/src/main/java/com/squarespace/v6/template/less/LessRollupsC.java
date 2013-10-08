package com.squarespace.v6.template.less;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;


public class LessRollupsC extends BaseCommand {

  private static final String PROGRAM_NAME = "sqs_rollups_lessc";

  private static final String IMPLNAME = "(Squarespace LESS Rollup Compiler) [Java, Squarespace]";

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
    LessRollupsC rollups = new LessRollupsC();
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
    if (args == null || args.isEmpty()) {
      System.err.println("you must provide rollups arguments");
      System.exit(1);
    }
    
  }
}
