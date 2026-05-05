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

import com.squarespace.less.exec.FunctionTable;
import com.squarespace.less.exec.LessEvaluator;
import com.squarespace.less.exec.LessRenderer;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.LessParser;
import com.squarespace.less.parse.LessSyntax;
import com.squarespace.less.plugins.ColorBlendingFunctions;
import com.squarespace.less.plugins.ColorChannelFunctions;
import com.squarespace.less.plugins.ColorDefinitionFunctions;
import com.squarespace.less.plugins.ColorOperationsFunctions;
import com.squarespace.less.plugins.ListFunctions;
import com.squarespace.less.plugins.MathFunctions;
import com.squarespace.less.plugins.MiscFunctions;
import com.squarespace.less.plugins.StringFunctions;
import com.squarespace.less.plugins.TypeFunctions;


/**
 * Singleton and main entry point for parse, compile and render capabilities.
 */
public class LessCompiler {

  /**
   * Current LESS language compatibility level of this compiler.
   */
  public static final String LESSJS_VERSION = "1.3.3";

  /**
   * Table of function implementations that will be used by the compiler.
   */
  private final FunctionTable functionTable;

  public LessCompiler() {
    this(defaultFunctionTable());
  }

  public LessCompiler(FunctionTable functionTable) {
    this.functionTable = functionTable;
    this.functionTable.setInUse();
  }

  public LessContext context(LessOptions opts) {
    LessContext ctx = new LessContext(opts);
    ctx.setCompiler(this);
    return ctx;
  }

  public FunctionTable functionTable() {
    return functionTable;
  }

  /**
   * Parse source. The compat level comes from the context options
   * (default: every legacy behavior active).
   */
  public Stylesheet parse(String raw, LessContext ctx) throws LessException {
    return parse0(raw, ctx, null, null, null, true);
  }

  /**
   * Parse source with the legacy safeMode flag.
   */
  public Stylesheet parse(String raw, LessContext ctx, boolean safeMode) throws LessException {
    return parse0(raw, ctx, null, null, safeMode, true);
  }

  /**
   * Parse the source into a stylesheet, putting the parser into safe mode by default.
   */
  public Stylesheet parse(String raw, LessContext ctx, Path rootPath, Path fileName) throws LessException {
    return parse0(raw, ctx, rootPath, fileName, null, true);
  }

  public Stylesheet parse(String raw, LessContext ctx, Path rootPath, Path fileName, boolean safeMode) throws LessException {
    return parse0(raw, ctx, rootPath, fileName, safeMode, true);
  }

  /**
   * Parse an imported stylesheet inside an ongoing top-level parse.
   *
   * <p>The importer consumes every not-yet-cached @import by recursing
   * into this entry, so an imported sheet is a continuation of the
   * current top-level parse, not a fresh one. Unlike the public
   * parse() overloads this entry does not call ctx.resetImporter():
   * resetting here would wipe the take() memo entries this compile has
   * already recorded, and a later import site of an earlier path would
   * take a second, non-identical deep copy. The reset runs exactly once
   * per top-level parse or compile, at the outermost call.
   */
  public Stylesheet parseImport(String raw, LessContext ctx, Path rootPath, Path fileName) throws LessException {
    return parse0(raw, ctx, rootPath, fileName, null, false);
  }

  private Stylesheet parse0(String raw, LessContext ctx, Path rootPath, Path fileName, Boolean safeMode,
      boolean resetImporter) throws LessException {
    // The legacy safeMode boolean is a context-scoped recovery override
    // (never written into the caller's LessOptions, so a shared options
    // object cannot be poisoned by one boolean-flag caller, and it
    // persists on this context across compiles. See
    // LessContext.safeModeOverride. Parse, evaluation and render all
    // read ctx.safeMode().
    if (safeMode != null) {
      ctx.safeModeOverride(safeMode);
    }
    // A prior parse or compile on a reused context against a shared
    // preCache may have memoized one deep copy per imported path. Reset
    // once per top-level parse/compile so this parse takes fresh copies.
    // Recursive import parses must not reset: they are part of the
    // current top-level parse and would drop the copies it already took.
    if (resetImporter) {
      ctx.resetImporter();
    }
    LessStats stats = ctx.stats();
    long started = stats.now();
    LessParser parser = new LessParser(ctx, raw, rootPath, fileName);
    Stylesheet sheet = null;
    try {
      sheet = (Stylesheet) parser.parse(LessSyntax.STYLESHEET);
    } finally {
      stats.parseDone(raw.length(), started);
    }
    return sheet;
  }

  public String render(Stylesheet stylesheet, LessContext ctx) throws LessException {
    LessEvaluator engine = new LessEvaluator(ctx);
    Stylesheet expanded = engine.evaluate(stylesheet);
    return LessRenderer.render(ctx, expanded);
  }

  public Stylesheet expand(Stylesheet stylesheet, LessContext ctx) throws LessException {
    LessEvaluator engine = new LessEvaluator(ctx);
    return engine.evaluate(stylesheet);
  }

  public String compile(String raw, LessContext ctx) throws LessException {
    return compile0(raw, ctx, null, null, null);
  }

  public String compile(String raw, LessContext ctx, Path rootPath, Path fileName) throws LessException {
    return compile0(raw, ctx, rootPath, fileName, null);
  }

  public String compile(String raw, LessContext ctx, Path rootPath, Path fileName, boolean safeMode) throws LessException {
    return compile0(raw, ctx, rootPath, fileName, safeMode);
  }

  private String compile0(String raw, LessContext ctx, Path rootPath, Path fileName, Boolean safeMode)
      throws LessException {
    // A prior failed compile may have left the depth counters nonzero.
    // Reset so a reused context does not fail fresh compiles.
    ctx.resetDepthCounters();
    // A prior compile that died after recording recovery warnings (the
    // renderer never drained them) would leak stale WARNING comments
    // into this compile's output and suppress identical fresh warnings
    // via the stale dedupe keys.
    ctx.resetWarnings();
    Stylesheet sheet = parse0(raw, ctx, rootPath, fileName, safeMode, true);
    LessStats stats = ctx.stats();
    long started = stats.now();
    String result = "";
    try {
      result = render(sheet, ctx);
    } finally {
      stats.compileDone(started);
    }
    return result;
  }

  /**
   * Builds the default function table.  Functions are stateless so this table
   * can be shared among many instances of the compiler.  This method provides
   * a convenient starting point which can be extended by registering additional
   * function packages.
   */
  public static FunctionTable defaultFunctionTable() {
    FunctionTable table = new FunctionTable();
    table.register(new ColorBlendingFunctions());
    table.register(new ColorChannelFunctions());
    table.register(new ColorDefinitionFunctions());
    table.register(new ColorOperationsFunctions());
    table.register(new ListFunctions());
    table.register(new MathFunctions());
    table.register(new MiscFunctions());
    table.register(new StringFunctions());
    table.register(new TypeFunctions());
    return table;
  }

}
