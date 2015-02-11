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

package com.squarespace.less.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessCompiler;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessErrorType;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.FunctionTable;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.LessStream;
import com.squarespace.less.parse.Parselet;
import com.squarespace.less.parse.Parselets;
import com.squarespace.less.plugins.DummyFunctions;
import com.squarespace.less.plugins.ext.ExtStringFunctions;


/**
 * Wrapper to simplify writing tests for the LESS compiler.
 */
public class LessHarness {

  private static final FunctionTable FUNCTIONS = LessCompiler.defaultFunctionTable();

  static {
    FUNCTIONS.register(new DummyFunctions());
    FUNCTIONS.register(new ExtStringFunctions());
  }

  private final LessCompiler compiler = new LessCompiler(FUNCTIONS);

  private final List<GenericBlock> definitions = new ArrayList<>();

  private final Parselet[] parselet;

  public LessHarness() {
    this.parselet = Parselets.STYLESHEET;
  }

  public LessHarness(GenericBlock defs) {
    this(Parselets.STYLESHEET, defs);
  }

  public LessHarness(Parselet[] parselet) {
    this.parselet = parselet;
  }

  public LessHarness(Parselet[] parselet, GenericBlock ... defs) {
    this.parselet = parselet;
    for (GenericBlock def : defs) {
      this.definitions.add(def);
    }
  }

  public LessContext context() {
    return context(null);
  }

  public LessContext context(LessOptions opts) {
    LessContext ctx = (opts == null) ? new LessContext() : new LessContext(opts);
    ctx.setCompiler(compiler);
    return ctx;
  }

  public String execute(String raw) throws LessException {
    return execute(raw, new LessOptions());
  }

  public String execute(String raw, LessOptions opts) throws LessException {
    try {
      return compiler.compile(raw, context(opts));
    } catch (LessException e) {
      fail(ErrorUtils.formatError(Paths.get("<source>"), e, 4));
      return null;
    }
  }

  public void executeFails(String raw, LessErrorType expected) {
    try {
      compiler.compile(raw, context(new LessOptions()));
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), expected);
    }
  }

  public Node parse(String raw) throws LessException {
    return parse(raw, parselet);
  }

  public void parseEquals(String raw, Node expected) throws LessException {
    try {
      Node res = parse(raw, parselet);
      assertEquals(res, expected, raw);
    } catch (LessException e) {
      fail(ErrorUtils.formatError(Paths.get("<source>"), e, 4));
    }
  }

  public void parseFails(String raw, LessErrorType expected) {
    try {
      parse(raw);
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), expected);
    }
  }

  public void renderEquals(String raw, String expected) throws LessException {
    ExecEnv env = define(definitions);
    Node res = evaluate(raw, parselet, env);
    assertEquals(env.context().render(res), expected, raw);
  }

  public void evalEquals(String raw, Node expected) throws LessException {
    Node res = evaluate(raw, parselet, define(definitions));
    assertEquals(res, expected, raw);
  }

  public void evalEquals(Node input, Node expected) throws LessException {
    Node result = evaluate(input, define(definitions));
    assertEquals(result, expected, input.repr());
  }

  public void evalFails(String raw, LessErrorType expected) throws LessException {
    try {
      evaluate(raw, parselet, define(definitions));
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), expected);
    }
  }

  public void evalFails(Node node, LessErrorType expected) throws LessException {
    try {
      evaluate(node, define(definitions));
      fail("Expected LessException of type " + expected);
    } catch (LessException e) {
      assertEquals(e.primaryError().type(), expected);
    }
  }

  public Node evaluate(String raw) throws LessException {
    return evaluate(parse(raw, parselet), define(definitions));
  }

  public Node evaluate(Node node) throws LessException {
    return node.eval(define(definitions));
  }

  public Node evaluate(Node node, ExecEnv env) throws LessException {
    return node.eval(env);
  }

  public Node evaluate(String raw, Parselet[] parselet) throws LessException {
    return evaluate(parse(raw, parselet), define(definitions));
  }

  public Node evaluate(String raw, Parselet[] parselet, ExecEnv env) throws LessException {
    return evaluate(parse(raw, parselet), env);
  }

  private Node parse(String raw, Parselet[] parselet) throws LessException {
    LessStream stm = new LessStream(context(), raw);
    Node res = stm.parse(parselet);
    stm.checkComplete();
    return res;
  }

  private ExecEnv define(List<GenericBlock> blocks) throws LessException {
    LessContext ctx = context();
    ExecEnv env = ctx.newEnv();
    // The first block pushed will be the top-most stack frame.
    for (GenericBlock block : blocks) {
      env.push(block);
    }
    return env;
  }

}
