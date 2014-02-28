package com.squarespace.v6.template.less;

import java.io.PrintStream;


public abstract class BaseCommand {

  protected static final String GLOB_LESS = "glob:*.less";
  
  protected static final String VERSION = "1.3.3";

  protected static final String SEPARATOR = 
      "\n==============================================================================\n";

  private static final Runtime RUNTIME = Runtime.getRuntime();

  private static final long MEGABYTE = 1024 * 1024;

  protected abstract String programName();
  
  protected PrintStream log(String msg) {
    System.err.print(programName());
    System.err.print(": ");
    System.err.print(msg);
    return System.err;
  }
  
  protected void emitMemory(String phase) {
    long maxMemory = RUNTIME.maxMemory();
    long usedMemory = RUNTIME.totalMemory() - RUNTIME.freeMemory();
    long usedMb = usedMemory / MEGABYTE;
    StringBuilder buf = new StringBuilder();
    buf.append(phase).append(" memory usage: ");
    buf.append(" max ").append(maxMemory / MEGABYTE).append(" MB");
    buf.append(", used ").append(usedMb).append(" MB");
    buf.append('\n');
    log(buf.toString());
  }

}
