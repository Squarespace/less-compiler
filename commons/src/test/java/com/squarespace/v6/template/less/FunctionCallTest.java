package com.squarespace.v6.template.less;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class FunctionCallTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);
    
    h.parseEquals("rgb(1,2,3)", call("rgb", dim(1), dim(2), dim(3)));
    h.parseEquals("foo-bar(@a)", call("foo-bar", var("@a")));

    Node str = quoted('"', "x", var("@y", true), "z");
    h.parseEquals("name(\"x@{y}z\")", call("name", str));

    Node foo_1 = assign("foo", dim(1));
    Node bar_2px = assign("bar", dim(2, Unit.PX));
    h.parseEquals("name(foo=1, bar=2px)", call("name", foo_1, bar_2px));
  }
  
}
