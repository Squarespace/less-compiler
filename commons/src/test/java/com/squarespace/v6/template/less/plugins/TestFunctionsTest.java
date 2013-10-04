package com.squarespace.v6.template.less.plugins;

import static com.squarespace.v6.template.less.ExecuteErrorType.FUNCTION_CALL;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.parse.Parselets;


public class TestFunctionsTest extends LessTestBase {

  @Test
  public void testFunction() throws LessException {
    GenericBlock defs = defs(
        def("@one", dim(1)),
        def("@three", dim(3))
    );
  
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL, defs);
    
    // Non-built-in functions.
    Node expected = call("foo", dim(1));
    h.evalEquals("foo(1)", expected);
    h.evalEquals("foo(0.5 + 0.5)", expected);
    h.evalEquals("foo(@one)", expected);
    h.evalEquals("foo(2-@one)", expected);
    
    // Assignment args
    expected = call("bar", assign("name", quoted('"', false, "bob")));
    h.evalEquals("bar(name=\"bob\")", expected);
    
    // wrong arg counts
    h.evalFails("dummy3(1, 2, 3, 4)", FUNCTION_CALL);
    h.evalFails("dummy3()", FUNCTION_CALL);
  }
  
}
