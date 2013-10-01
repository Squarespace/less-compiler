package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.ExecuteErrorType.VAR_CIRCULAR_REFERENCE;
import static com.squarespace.v6.template.less.ExecuteErrorType.VAR_UNDEFINED;
import static com.squarespace.v6.template.less.SyntaxErrorType.INCOMPLETE_PARSE;
import static com.squarespace.v6.template.less.parse.Parselets.QUOTED;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE_CURLY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.model.Unit;


public class VariableTest extends LessTestBase {
  
  @Test
  public void testEquals() {
    assertEquals(var("@a"), var("@a"));
    
    assertNotEquals(var("@a"), null);
    assertNotEquals(var("@a"), anon("@a"));
    assertNotEquals(var("@a"), var("@b"));
  }
  
  @Test
  public void testModelReprSafety() {
    var("@a").toString();
  }
 
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(VARIABLE);
    h.parseFails("@", INCOMPLETE_PARSE);
    h.parseFails("@-", INCOMPLETE_PARSE);
    h.parseEquals("@1", var("@1"));
    h.parseEquals("@a", var("@a"));
    h.parseEquals("@@a", var("@@a"));
    
    h = new LessHarness(VARIABLE_CURLY);
    h.parseEquals("@{", null);
    h.parseFails("@{a", INCOMPLETE_PARSE);
    h.parseFails("@{aa", INCOMPLETE_PARSE);
    h.parseEquals("@{aa}", var("@aa", true));
  }
  
  @Test
  public void testUndefined() throws LessException {
    LessHarness h = new LessHarness();
    h.executeFails(".foo { color: @c; }", VAR_UNDEFINED);
  }
  
  @Test
  public void testSelfReference() throws LessException {
    GenericBlock defs = defs(
        def("@value", var("@link1")),
        def("@link1", var("@link2")),
        def("@link2", var("@value"))
        );
    LessHarness h = new LessHarness(VARIABLE, defs);
    h.evalFails("@value", VAR_CIRCULAR_REFERENCE);
    h.evalFails("@link1", VAR_CIRCULAR_REFERENCE);
    h.evalFails("@link2", VAR_CIRCULAR_REFERENCE);
  }
 
  @Test
  public void testIndirectReference() throws LessException {
    GenericBlock defs = defs(
        def("@value", dim(12, Unit.PX)),
        def("@real", quoted('"', var("@value"))),
        def("@one", kwd("real")),
        def("@two", quoted(Chars.NULL, "real"))
        );
    LessHarness h = new LessHarness(VARIABLE, defs);
    h.renderEquals("@@one", "\"12px\"");
    h.renderEquals("@@two", "\"12px\"");
  }
  
  @Test
  public void testDereference() throws LessException {
    GenericBlock defs = defs(
        def("@var", var("@link1")),
        def("@link1", var("@white")),
        def("@white", color("#fff")),
        def("@color", color("#123")),
        def("@num", dim(1, Unit.PX))
        );
    LessHarness h = new LessHarness(VARIABLE, defs);
    h.renderEquals("@var", "white");
    h.renderEquals("@color", "#123");
    h.renderEquals("@num", "1px");
  }
  
  @Test
  public void testEmbeddedReferences() throws LessException {
    GenericBlock defs = defs(
        def("@a", dim(1, Unit.EM)),
        def("@b", quoted('"', anon("foo"))),
        def("@c", dim(2, Unit.PX)),
        def("@d", quoted('"', anon("\"bar\""))),
        def("@e", color("#123456"))
        );
    LessHarness h = new LessHarness(QUOTED, defs);
    h.renderEquals("'@{a} @{b} @{c} @{d} @{e}", "'1em foo 2px \"bar\" #123456'");
  }
 
  @Test
  public void testNestedVariables() throws LessException {
    Options opts = new Options(true);
    LessHarness h = new LessHarness();

    String str = "@a: 1px; .x { @b: #111; .y { .z { key: @a @b; } } }";
    assertEquals(h.execute(str, opts), ".x .y .z{key:1px #111;}\n");

    str = "@a: 1; .x { @a: 2; .y { @a: 3; .z { key: @a; } } }";
    assertEquals(h.execute(str, opts), ".x .y .z{key:3;}\n");
  }

}
