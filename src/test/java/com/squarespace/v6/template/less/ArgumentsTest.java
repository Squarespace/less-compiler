package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Operator.DIVIDE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Argument;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class ArgumentsTest extends LessTestBase {

  @Test
  public void testEquals() {
    Argument arg_xy = arg("x", anon("y"));
    Argument arg_xz = arg("x", anon("z"));

    assertEquals(args(';'), args(';'));
    assertEquals(args(';', arg_xy), args(';', arg_xy));
    assertEquals(args(',', arg_xy, arg_xz), args(',', arg_xy, arg_xz));

    assertNotEquals(args(','), null);
    assertNotEquals(args(','), args(';'));
    assertNotEquals(args(','), args(';', arg_xz));
    assertNotEquals(args(',', arg_xy), args(';', arg_xz));
  }
  
  @Test
  public void testModelReprSafety() {
    arg(null, anon("z")).toString();
    arg("y", anon("z")).toString();
  }

  @Test
  public void testArguments() throws LessException {
    LessHarness h = new LessHarness(Parselets.MIXIN_CALL_ARGS);
    
    h.parseEquals("()",
        args(','));
    
    h.parseEquals("(@b)", 
        args(',', arg(null, var("@b"))));
    
    h.parseEquals("(@b: 12px)", 
        args(',', arg("@b", dim(12, Unit.PX))));
    
    h.parseEquals("('@{x} y @{z}')",
        args(',', arg(null, quoted('\'', false, var("@x", true), anon(" y "), var("@z", true)))));
    
    h.parseEquals("(@a @b, @c)",
        args(',', arg(null, expn(var("@a"), var("@b"))), arg(null, var("@c"))));
    
    h.parseEquals("(@a: 1,2; @b: 2)",
        args(';', arg("@a", expnlist(dim(1), dim(2))), arg("@b", dim(2))));
            
    h.parseEquals("(1,2; @b)",
        args(';', arg(null, expnlist(dim(1), dim(2))), arg(null, var("@b"))));
    
    h.parseEquals("(1; 2/16;)",
        args(';', arg(null, dim(1)), arg(null, oper(DIVIDE, dim(2), dim(16)))));
  }
  
}
