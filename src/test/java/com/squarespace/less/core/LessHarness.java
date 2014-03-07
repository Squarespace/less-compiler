package com.squarespace.less.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.squarespace.less.Context;
import com.squarespace.less.ErrorType;
import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessException;
import com.squarespace.less.Options;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.FunctionTable;
import com.squarespace.less.exec.LessEvaluator;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.LessStream;
import com.squarespace.less.parse.Parselet;
import com.squarespace.less.parse.Parselets;
import com.squarespace.less.plugins.TestFunctions;


/**
 * Wrapper to simplify writing tests for the LESS compiler.
 */
public class LessHarness {

  private static final FunctionTable FUNCTIONS = LessCompiler.defaultFunctionTable();

  static {
    FUNCTIONS.register(new TestFunctions());
  }

  private GenericBlock defs;

  private Parselet[] parselet;

  public LessHarness() {
    this(Parselets.STYLESHEET, null);
  }

  public LessHarness(GenericBlock defs) {
    this(Parselets.STYLESHEET, defs);
  }

  public LessHarness(Parselet[] parselet) {
    this(parselet, null);
  }

  public LessHarness(Parselet[] parselet, GenericBlock defs) {
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
    LessEvaluator engine = new LessEvaluator(ctx);
    Node res = parse(raw, parselet);
    return engine.render((Stylesheet)res);
  }

  public void executeFails(String raw, ErrorType expected) {
    try {
      execute(raw);
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), expected);
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
      assertEquals(e.primaryError().type(), expected);
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
      assertEquals(e.primaryError().type(), expected);
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

  public Node evaluate(String raw, Parselet[] parselet) throws LessException {
    return evaluate(parse(raw, parselet), define(defs));
  }

  public Node evaluate(String raw, Parselet[] parselet, ExecEnv env) throws LessException {
    return evaluate(parse(raw, parselet), env);
  }

  private Node parse(String raw, Parselet[] parselet) throws LessException {
    LessStream stm = new LessStream(raw);
    Node res = stm.parse(parselet);
    stm.checkComplete();
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
