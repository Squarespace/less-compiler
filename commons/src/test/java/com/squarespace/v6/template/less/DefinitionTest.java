package com.squarespace.v6.template.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class DefinitionTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(def("@x", anon("y")), def("@x", anon("y")));
    assertNotEquals(def("@y", anon("y")), def("@x", anon("y")));
    assertNotEquals(def("@x", anon("y")), def("@y", anon("y")));
  }
  
  @Test
  public void testModelReprSafety() {
    def("@x", anon("y")).toString();
  }
  
  @Test
  public void testDefinition() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);

    h.parseEquals("@foo: 12px", def("@foo", dim(12, Unit.PX)));
    h.parseEquals("@foo: 'bar';", def("@foo", quoted('\'', "bar")));
    h.parseEquals("@a: @b;", def("@a", var("@b")));
    h.parseEquals("@bpMedium: 1000px;", def("@bpMedium", dim(1000, Unit.PX)));
  }

}
