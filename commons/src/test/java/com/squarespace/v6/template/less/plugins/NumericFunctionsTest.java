package com.squarespace.v6.template.less.plugins;

import static com.squarespace.v6.template.less.ExecuteErrorType.FUNCTION_CALL;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class NumericFunctionsTest extends LessTestBase {

  @Test
  public void testFunctions() throws LessException {
    LessHarness h = new LessHarness(Parselets.FUNCTION_CALL);
    
    // Decimal to percentage
    h.evalEquals("percentage(1)", dim(100, Unit.PERCENTAGE));
    h.evalEquals("percentage(.25)", dim(25, Unit.PERCENTAGE));
    h.evalEquals("percentage(0.0)", dim(0, Unit.PERCENTAGE));
    h.evalEquals("percentage(-2)", dim(-200, Unit.PERCENTAGE));
    
    // Rounding
    h.evalEquals("round(-12.2)", dim(-12));
    h.evalEquals("round(1.57, -5)", dim(2));
    h.evalEquals("round(2.1)", dim(2));
    h.evalEquals("round(3.4px)", dim(3, Unit.PX));
    h.evalEquals("round(3.5px)", dim(4, Unit.PX));
    h.evalEquals("round(3.5px, 1)", dim(3.5, Unit.PX));
    h.evalEquals("round(3.55px, 1)", dim(3.6, Unit.PX));
    h.evalEquals("round(-2.55, 2)", dim(-2.55));
    h.evalEquals("round(-2.57, 1)", dim(-2.6));
    h.evalEquals("round(-2.55, 1)", dim(-2.5));
    h.evalEquals("round(-2.52, 1)", dim(-2.5));
    h.evalEquals("round(12.123%, 1)", dim(12.1, Unit.PERCENTAGE));
    
    // Unit changes
    h.evalEquals("unit(1, px)", dim(1, Unit.PX));
    h.evalEquals("unit(3em, pt)", dim(3, Unit.PT));
    h.evalFails("unit('foo', px)", FUNCTION_CALL);
    h.evalFails("unit(3em, quark)", FUNCTION_CALL);
    
  }

}
