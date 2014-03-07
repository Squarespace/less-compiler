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

import static com.squarespace.less.LessCompiler.VERSION;

import java.nio.file.Path;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.StackFormatter;


public class LessMessages {

  private static final String SQUARESPACE_LESS_HEADER = "\n/* Squarespace LESS Compiler " + VERSION + " */\n";

  private static final int DEFAULT_ERROR_INDENT = 4;

  private static final int DEFAULT_STACK_WINDOW = 6;

  private final int errorIndent;

  private final int stackWindow;

  public LessMessages() {
    this(DEFAULT_ERROR_INDENT, DEFAULT_STACK_WINDOW);
  }

  public LessMessages(int errorIndent, int stackWindow) {
    this.errorIndent = errorIndent;
    this.stackWindow = stackWindow;
  }

  public void successHeader(Buffer buf) {
    buf.append(SQUARESPACE_LESS_HEADER);
  }

  public void errorHeader(Buffer buf) {
    buf.append("\n\nSQUARESPACE_LESS_ERROR\n\n");
  }

  public String formatStats(LessStats stats, Path path) {
    return formatStats(new Buffer(0), stats, path).toString();
  }

  public Buffer formatStats(Buffer buf, LessStats stats, Path path) {
    buf.append("/* --------------------------------------------------------\n");
    buf.append("Squarespace LESS compiler ").append(VERSION).append(" Statistics for '");
    buf.append(path.toString()).append("':\n");
    buf.append("    parse time: ").append(formatMs(stats.parseTimeMs())).append('\n');
    buf.append("  compile time: ").append(formatMs(stats.compileTimeMs())).append('\n');
    buf.append("disk wait time: ").append(formatMs(stats.diskWaitTimeMs())).append('\n');
    buf.append("  import count: ").append(stats.importCount()).append('\n');
    buf.append("---------------------------------------------------------- */\n");
    return buf;
  }

  public String formatError(LessException exc) {
    Buffer buf = new Buffer(errorIndent);
    errorHeader(buf);
    StackFormatter fmt = new StackFormatter(exc.errorContext(), 4, stackWindow);
    buf.append(fmt.format()).append('\n');
    buf.append(exc.primaryError().getMessage());
    buf.append('\n');
    return buf.toString();
  }

  private String formatMs(double ms) {
    return String.format("%.3fms", ms);
  }

}
