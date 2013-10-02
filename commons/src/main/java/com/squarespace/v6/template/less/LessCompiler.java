package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

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
    return parse(raw, ctx, null);
  }
  
  public Stylesheet parse(String raw, Context ctx, Path rootPath) throws LessException {
    LessStats stats = ctx.stats();
    long started = stats.now();
    LessStream stm = new LessStream(raw, rootPath);
    Stylesheet sheet = null;
    try {
      sheet = (Stylesheet) stm.parse(Parselets.STYLESHEET);
    } catch (LessException e) {
      System.err.println(stm.errorMessage(e));
    }
    if (!stm.complete()) {
      String remainder = stm.remainder();
      if (!remainder.isEmpty()) {
        throw new LessException(error(INCOMPLETE_PARSE).arg0(StringEscapeUtils.escapeJava(remainder)));
      }
    }
    stats.parseDone(raw.length(), started);
    return sheet == null ? null : sheet.copy();
  }
  
  public Stylesheet expand(Stylesheet stylesheet, Context ctx) throws LessException {
    LessEngine engine = new LessEngine();
    return engine.expand(stylesheet, ctx);
  }
  
  public String compile(String raw, Context ctx) throws LessException {
    return compile(raw, ctx, null);
  }
  
  public String compile(String raw, Context ctx, Path rootPath) throws LessException {
    Stylesheet sheet = parse(raw, ctx, rootPath);
    LessStats stats = ctx.stats();
    long started = stats.now();
    LessEngine cm = new LessEngine();
    String result = cm.render(sheet, ctx);
    stats.compileDone(started);
    return result;
  }
  
  public String readFile(Path path) {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
    } catch (IOException e) {
      String message = String.format("Failure to read from '%s'", path);
      throw new RuntimeException(message + ": " + e.getMessage(), e);
    }
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
