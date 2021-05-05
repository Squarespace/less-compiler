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
import com.squarespace.less.parse2.LessParser;
import com.squarespace.less.parse2.LessSyntax;
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

  public Stylesheet parse(String raw, LessContext ctx) throws LessException {
    return parse(raw, ctx, null, null, false);
  }

  public Stylesheet parse(String raw, LessContext ctx, boolean safeMode) throws LessException {
    return parse(raw, ctx, null, null, safeMode);
  }

  public Stylesheet parse(String raw, LessContext ctx, Path rootPath, Path fileName) throws LessException {
    return parse(raw, ctx, rootPath, fileName, false);
  }

  public Stylesheet parse(String raw, LessContext ctx, Path rootPath, Path fileName, boolean safeMode) throws LessException {
    LessStats stats = ctx.stats();
    long started = stats.now();
    LessParser parser = new LessParser(ctx, raw, rootPath, fileName);
    parser.safeMode(safeMode);
    Stylesheet sheet = (Stylesheet) parser.parse(LessSyntax.STYLESHEET);
    stats.parseDone(raw.length(), started);
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
    return compile(raw, ctx, null, null);
  }

  public String compile(String raw, LessContext ctx, Path rootPath, Path fileName) throws LessException {
    Stylesheet sheet = parse(raw, ctx, rootPath, fileName);
    LessStats stats = ctx.stats();
    long started = stats.now();
    String result = render(sheet, ctx);
    stats.compileDone(started);
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
