package com.squarespace.v6.template.less;

import java.util.EnumSet;
import java.util.Set;


/**
 * Represents all basic options for the compiler.
 */
public class Options {

  public static final int DEFAULT_INDENT = 2;

  public static final String DEFAULT_ROOT = ".";
  
  public static final int DEFAULT_RECURSION_LIMIT = 64;
  
  private final Set<Option> flags = EnumSet.noneOf(Option.class);
  
  private int indent = DEFAULT_INDENT;

  private String importRoot = DEFAULT_ROOT;
  
  private int recursionLimit = DEFAULT_RECURSION_LIMIT;
  
  public Options() {
  }
  
  public Options(int indent) {
    compress(false);
    indent(indent);
  }
  
  public Options(boolean compress) {
    compress(compress);
  }
  
  public boolean compress() {
    return flags.contains(Option.COMPRESS);
  }
  
  public boolean debug() {
    return flags.contains(Option.DEBUG);
  }

  public int indent() {
    return indent;
  }

  public boolean importOnce() {
    return flags.contains(Option.IMPORT_ONCE);
  }
  
  public String importRoot() {
    return importRoot;
  }

  public boolean lineNumbers() {
    return flags.contains(Option.LINE_NUMBERS);
  }
  
  public int recursionLimit() {
    return recursionLimit;
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

  public void importOnce(boolean flag) {
    set(flag, Option.IMPORT_ONCE);
  }
  
  public void importRoot(String root) {
    this.importRoot = root;
  }

  public void lineNumbers(boolean flag) {
    set(flag, Option.LINE_NUMBERS);
  }
  
  public void recursionLimit(int limit) {
    this.recursionLimit = limit;
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
  
  private static enum Option {
    COMPRESS,
    DEBUG,
    IMPORT_ONCE,
    LINE_NUMBERS,
    TABS,
    TRACING
  }

}
