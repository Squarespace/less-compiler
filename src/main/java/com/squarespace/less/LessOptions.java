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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


/**
 * Represents all basic options for the compiler.
 */
public class LessOptions {

  public static final int DEFAULT_INDENT = 2;

  public static final String DEFAULT_ROOT = ".";

  public static final int DEFAULT_RECURSION_LIMIT = 64;

  private final Set<Option> flags = EnumSet.of(Option.STRICT);

  private final List<Path> importPaths = new ArrayList<>();

  private int indent = DEFAULT_INDENT;

  private int mixinRecursionLimit = DEFAULT_RECURSION_LIMIT;

  private int importRecursionLimit = DEFAULT_RECURSION_LIMIT;

  public LessOptions() {
  }

  public LessOptions(int indent) {
    compress(false);
    indent(indent);
  }

  public LessOptions(boolean compress) {
    compress(compress);
  }

  public boolean compress() {
    return flags.contains(Option.COMPRESS);
  }

  public boolean debug() {
    return flags.contains(Option.DEBUG);
  }

  public boolean hideWarnings() {
    return flags.contains(Option.HIDE_WARNINGS);
  }

  public int indent() {
    return indent;
  }

  public boolean ignoreComments() {
    return flags.contains(Option.IGNORE_COMMENTS);
  }

  public boolean importOnce() {
    return flags.contains(Option.IMPORT_ONCE);
  }

  public List<Path> importPaths() {
    return importPaths;
  }

  public boolean lineNumbers() {
    return flags.contains(Option.LINE_NUMBERS);
  }

  public int importRecursionLimit() {
    return importRecursionLimit;
  }

  public int mixinRecursionLimit() {
    return mixinRecursionLimit;
  }

  public boolean strict() {
    return flags.contains(Option.STRICT);
  }

  public boolean tabs() {
    return flags.contains(Option.TABS);
  }

  public boolean tracing() {
    return flags.contains(Option.TRACING);
  }

  public void compress(boolean flag) {
    set(flag, Option.COMPRESS);
  }

  public void debug(boolean flag) {
    set(flag, Option.DEBUG);
  }

  public void indent(int size) {
    this.indent = size;
  }

  public void ignoreComments(boolean flag) {
    set(flag, Option.IGNORE_COMMENTS);
  }

  public void importOnce(boolean flag) {
    set(flag, Option.IMPORT_ONCE);
  }

  public void addImportPath(String path) {
    this.importPaths.add(Paths.get(path));
  }

  public void hideWarnings(boolean flag) {
    set(flag, Option.HIDE_WARNINGS);
  }

  public void importPaths(List<String> paths) {
    if (paths != null) {
      for (String path : paths) {
        this.importPaths.add(Paths.get(path));
      }
    }
  }

  public void lineNumbers(boolean flag) {
    set(flag, Option.LINE_NUMBERS);
  }

  public void importRecursionLimit(int limit) {
    this.importRecursionLimit = limit;
  }

  public void mixinRecursionLimit(int limit) {
    this.mixinRecursionLimit = limit;
  }

  public void strict(boolean flag) {
    set(flag, Option.STRICT);
  }

  public void tabs(boolean flag) {
    set(flag, Option.TABS);
  }

  public void tracing(boolean flag) {
    set(flag, Option.TRACING);
  }

  private void set(boolean flag, Option opt) {
    if (flag) {
      flags.add(opt);
    } else {
      flags.remove(opt);
    }
  }

  private enum Option {
    COMPRESS,
    DEBUG,
    HIDE_WARNINGS,
    IGNORE_COMMENTS,
    IMPORT_ONCE,
    LINE_NUMBERS,
    STRICT,
    TABS,
    TRACING
  }

}
