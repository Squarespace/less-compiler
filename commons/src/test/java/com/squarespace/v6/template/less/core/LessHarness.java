package com.squarespace.v6.template.less.core;

import static com.squarespace.v6.template.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.ErrorType;
import com.squarespace.v6.template.less.LessCompiler;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.FunctionTable;
import com.squarespace.v6.template.less.exec.LessEngine;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Stylesheet;
import com.squarespace.v6.template.less.parse.LessStream;
import com.squarespace.v6.template.less.parse.Parselet;
import com.squarespace.v6.template.less.parse.Parselets;
import com.squarespace.v6.template.less.plugins.TestFunctions;


/**
 * Wrapper to simplify writing tests for the LESS compiler.
 */
public class LessHarness {

  private static final FunctionTable FUNCTIONS = LessCompiler.buildFunctionTable();
  
  static {
    FUNCTIONS.register(new TestFunctions());
  }

  private GenericBlock defs;
  
  private Parselet parselet;
  
  public LessHarness() {
    this(Parselets.STYLESHEET, null);
  }

  public LessHarness(GenericBlock defs) {
    this(Parselets.STYLESHEET, defs);
  }

  public LessHarness(Parselet parselet) {
    this(parselet, null);
  }

  public LessHarness(Parselet parselet, GenericBlock defs) {
    this.parselet = parselet;
    this.defs = defs;
  }
  
  public Context context() {
    Context ctx = new Context();
    ctx.setFunctionTable(FUNCTIONS);
    return ctx;
  }
  
  public Context context(Options opts) {
    Context ctx = new Context(opts);
    ctx.setFunctionTable(FUNCTIONS);
    return ctx;
  }

  public String execute(String raw) throws LessException {
    return execute(raw, new Options());
  }
  
  public String execute(String raw, Options opts) throws LessException {
    Context ctx = context(opts);
    ctx.setCompiler(new LessCompiler());
    LessEngine engine = new LessEngine();
    Node res = parse(raw, parselet);
    return engine.render((Stylesheet)res, ctx);
  }
  
  public void executeFails(String raw, ErrorType expected) {
    try {
      execute(raw);
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.errorInfo().type(), expected);
    }
  }
  
  public Node parse(String raw) throws LessException {
    return parse(raw, parselet);
  }

  public void parseEquals(String raw, Node expected) throws LessException {
    Node res = parse(raw, parselet);
    assertEquals(res, expected, raw);
  }

  public void parseFails(String raw, ErrorType expected) {
    try {
      parse(raw);
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.errorInfo().type(), expected);
    }
  }
  
  public void renderEquals(String raw, String expected) throws LessException {
    ExecEnv env = define(defs);
    Node res = evaluate(raw, parselet, env);
    assertEquals(env.context().render(res), expected, raw);
  }
  
  public void evalEquals(String raw, Node expected) throws LessException {
    Node res = evaluate(raw, parselet, define(defs));
    assertEquals(res, expected, raw);
  }
  
  public void evalFails(String raw, ErrorType expected) throws LessException {
    try {
      evaluate(raw, parselet, define(defs));
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.errorInfo().type(), expected);
    }
  }

  public Node evaluate(String raw) throws LessException {
    return evaluate(parse(raw, parselet), define(defs));
  }
  
  public Node evaluate(Node node) throws LessException {
    return node.eval(define(defs));
  }
  
  public Node evaluate(Node node, ExecEnv env) throws LessException {
    return node.eval(env);
  }
  
  public Node evaluate(String raw, Parselet parselet) throws LessException {
    return evaluate(parse(raw, parselet), define(defs));
  }
  
  public Node evaluate(String raw, Parselet parselet, ExecEnv env) throws LessException {
    return evaluate(parse(raw, parselet), env);
  }
  
  private Node parse(String raw, Parselet parselet) throws LessException {
    LessStream stm = new LessStream(raw);
    Node res = stm.parse(parselet);
    String remainder = stm.remainder();
    if (!remainder.equals("")) {
      throw new LessException(error(INCOMPLETE_PARSE).arg0(StringEscapeUtils.escapeJava(remainder)));
    }
    return res;
  }

  private ExecEnv define(GenericBlock defs) throws LessException {
    Context ctx = context();
    ExecEnv env = ctx.newEnv();
    if (defs != null) {
      env.push(defs);
    }
    return env;
  }

}
