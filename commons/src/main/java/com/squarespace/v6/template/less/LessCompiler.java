package com.squarespace.v6.template.less;

import java.nio.file.Path;

import com.squarespace.v6.template.less.exec.FunctionTable;
import com.squarespace.v6.template.less.exec.LessEngine;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.parse.LessStream;
import com.squarespace.v6.template.less.parse.Parselets;
import com.squarespace.v6.template.less.plugins.ColorAdjustmentFunctions;
import com.squarespace.v6.template.less.plugins.ColorCombinationFunctions;
import com.squarespace.v6.template.less.plugins.ColorHSLFunctions;
import com.squarespace.v6.template.less.plugins.ColorRGBFunctions;
import com.squarespace.v6.template.less.plugins.GeneralFunctions;
import com.squarespace.v6.template.less.plugins.NumericFunctions;
import com.squarespace.v6.template.less.plugins.TypeFunctions;


/**
 * Singleton and main entry point for parse, compile and render capabilities.
 */
public class LessCompiler {
  
  private static final FunctionTable DEFAULT_FUNCTIONS = buildFunctionTable();
  
  private FunctionTable functionTable;

  public LessCompiler() {
    functionTable = DEFAULT_FUNCTIONS;
  }

  public LessCompiler(FunctionTable functionTable) {
    this.functionTable = functionTable;
  }
  
  public Context context(Options opts) {
    Context ctx = new Context(opts);
    ctx.setCompiler(this);
    return ctx;
  }
  
  public FunctionTable functionTable() {
    return functionTable;
  }
  
  public Stylesheet parse(String raw, Context ctx) throws LessException {
    return parse(raw, ctx, null, null);
  }
  
  public Stylesheet parse(String raw, Context ctx, Path rootPath, Path fileName) throws LessException {
    LessStats stats = ctx.stats();
    long started = stats.now();
    LessStream stm = new LessStream(raw, rootPath, fileName);
    Stylesheet sheet = null;
    sheet = (Stylesheet) stm.parse(Parselets.STYLESHEET);
    stats.parseDone(raw.length(), started);
    return sheet == null ? null : sheet;
  }

  public String render(Stylesheet stylesheet, Context ctx) throws LessException {
    LessEngine engine = new LessEngine(ctx);
    return engine.render(stylesheet);
  }
  
  public Stylesheet expand(Stylesheet stylesheet, Context ctx) throws LessException {
    LessEngine engine = new LessEngine(ctx);
    return engine.expand(stylesheet);
  }
  
  public String compile(String raw, Context ctx) throws LessException {
    return compile(raw, ctx, null, null);
  }
  
  public String compile(String raw, Context ctx, Path rootPath, Path fileName) throws LessException {
    Stylesheet sheet = parse(raw, ctx, rootPath, fileName);
    LessStats stats = ctx.stats();
    long started = stats.now();
    LessEngine cm = new LessEngine(ctx);
    String result = cm.render(sheet);
    stats.compileDone(started);
    return result;
  }

  /**
   * Build the core function table.  The functions are stateless so this table
   * can be shared among many instances of the compiler.  This method provides
   * a convenient core table, which can be extended by registering further
   * function packages.
   */
  public static FunctionTable buildFunctionTable() {
    FunctionTable table = new FunctionTable();
    table.register(new ColorAdjustmentFunctions());
    table.register(new ColorCombinationFunctions());
    table.register(new ColorHSLFunctions());
    table.register(new ColorRGBFunctions());
    table.register(new GeneralFunctions());
    table.register(new NumericFunctions());
    table.register(new TypeFunctions());
    return table;
  }

}
