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

import java.io.PrintStream;

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessStats;
import com.squarespace.less.cli.LessC.Args;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.model.Stylesheet;


abstract class BaseCompile {

  public static final int OK = 0;

  public static final int ERR = 1;

  protected static final String SEPARATOR =
      "\n==============================================================================\n";

  protected static final long MEGABYTE = 1024 * 1024;

  protected final LessCompiler compiler = new LessCompiler();

  protected final Args args;

  protected final PrintStream out;

  protected final PrintStream err;

  public BaseCompile(Args args, PrintStream out, PrintStream err) {
    this.args = args;
    this.out = out;
    this.err = err;
  }

  public abstract int process();

  protected String canonicalize(Stylesheet stylesheet) {
    Buffer buf = new Buffer(args.compilerOptions().indent());
    stylesheet.repr(buf);
    return buf.toString();
  }

  protected String syntaxTree(Stylesheet stylesheet) {
    Buffer buf = new Buffer(args.compilerOptions().indent());
    stylesheet.modelRepr(buf);
    return buf.toString();
  }

  protected void emitStats(LessStats stats) {
    log(" Statistics:\n");
    log("     parse time: " + stats.parseTimeMs() + "ms\n");
    log("   compile time: " + stats.compileTimeMs() + "ms\n");
    log(" disk wait time: " + stats.diskWaitTimeMs() + "ms\n");
    log("   import count: " + stats.importCount() + "\n");
  }

  protected void emitMemory(String phase) {
    Runtime runtime = Runtime.getRuntime();
    long maxMemory = runtime.maxMemory();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    long usedMb = usedMemory / MEGABYTE;
    StringBuilder buf = new StringBuilder();
    buf.append(phase).append(" memory stats: ");
    buf.append(" max ").append(maxMemory / MEGABYTE).append(" MB");
    buf.append(", used ").append(usedMb).append(" MB");
    buf.append('\n');
    log(buf.toString());
  }

  protected int fail(String msg) {
    log(msg);
    return ERR;
  }

  protected void logElapsed(String prefix, long start, long end) {
    double compileElapsed = (end - start) / 1000000.0;
    err.printf("%s %.3fms\n", prefix, compileElapsed);
  }

  protected void log(String msg) {
    err.print(args.programName());
    err.print(": ");
    err.print(msg);
  }

}
