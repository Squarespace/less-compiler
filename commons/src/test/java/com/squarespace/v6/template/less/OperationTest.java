package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.ExecuteErrorType.PERCENT_MATH_ORDER;
import static com.squarespace.v6.template.less.model.Operator.ADD;
import static com.squarespace.v6.template.less.model.Operator.DIVIDE;
import static com.squarespace.v6.template.less.model.Operator.MULTIPLY;
import static com.squarespace.v6.template.less.model.Operator.SUBTRACT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.model.Operator;
import com.squarespace.v6.template.less.model.Unit;
import com.squarespace.v6.template.less.parse.Parselets;


public class OperationTest extends LessTestBase {
  
  @Test
  public void testEquals() {
    assertEquals(oper(ADD, dim(1), dim(2)), oper(ADD, dim(1), dim(2)));
    
    assertNotEquals(oper(ADD, dim(1), dim(2)), oper(SUBTRACT, dim(1), dim(2)));
    assertNotEquals(oper(ADD, dim(1), dim(2)), oper(ADD, dim(1), dim(3)));
  }
  
  @Test
  public void testModelReprSafety() {
    oper(ADD, dim(1), dim(2)).toString();
  }
  
  @Test
  public void testColorMath() throws LessException {
    LessHarness h = new LessHarness(Parselets.ADDITION);
    
    // color math
    h.evalEquals("#000 + 10", color("#0a0a0a"));
    h.evalEquals("#fff - 1", color("#fefefe"));
    h.evalEquals("#222 * 2", color("#444444"));
    h.evalEquals("#888 - #555 - #111", color("#222"));
    h.evalEquals("1 + blue + #010101", color("#0202ff"));
    h.evalEquals("1 * #123 * 1", color("#123"));
    h.evalEquals("#000 + red + blue", color("#f0f"));
  }
  
  @Test
  public void testMath() throws LessException {
    GenericBlock defs = defs(
        def("@two", dim(2)),
        def("@ten", oper(Operator.MULTIPLY, var("@two"), dim(5))),
        def("@tenPX", dim(10, Unit.PX)),
        def("@tenIN", dim(10, Unit.IN))
    );

    LessHarness h = new LessHarness(Parselets.ADDITION, defs);
    
    // whitespace
    h.evalEquals("(1+2)/(3*4)", dim(0.25));
    h.evalEquals("\n ( \n 2 \n + \n 3 \n ) \n", dim(5));
    
    // basic math
    h.evalEquals(".2 * 5", dim(1.0));
    h.evalEquals("2 + 3.1", dim(5.1));
    h.evalEquals(".2 * 6", dim(.2 * 6));
    h.evalEquals("1 + (2 + (-17)) + 3.14", dim(-10.86));
    h.evalEquals("1 + 2 - 7", dim(-4));
    h.evalEquals("-7 - -3 - -1", dim(-3));
    h.evalEquals("12 / 3", dim(4));
    h.evalEquals("(-(10)) * (-(100)) * (-(1000))", dim(-1000000));
    
    // variables
    h.evalEquals("@ten * 2", dim(20));
    h.evalEquals("-10000 / @ten", dim(-1000));
    h.evalEquals("-@ten * 32", dim(-320));
    
    // implicit units
    h.evalEquals("2 * 1px", dim(2, Unit.PX));
    h.evalEquals("7 + 3em + 1em", dim(11, Unit.EM));
    h.evalEquals("3cm - 4 + 4", dim(3, Unit.CM));
    
    // unit conversions
    h.evalEquals(".5in + 48px", dim(1, Unit.IN));
    h.evalEquals("30ms + 1s + 200ms", dim(1230, Unit.MS));
    h.evalEquals("180deg - .25turn", dim(90, Unit.DEG));
    h.evalEquals("10khz - 9000hz", dim(1, Unit.KHZ));
    
    // unit conversions with variables
    h.evalEquals("1px + @tenIN + @tenPX", dim(971, Unit.PX));
    h.evalEquals("@tenIN + 48px", dim(10.5, Unit.IN));
    
    // incomplete, ignored trailing operators
    h.evalEquals("1+2*", dim(3));
    
    // percentages
    h.evalEquals("100% * 10px", dim(10, Unit.PX));
    h.evalEquals("10% * 10px", dim(1, Unit.PX));
    h.evalEquals("10em * 50%", dim(5, Unit.EM));
    
    h.evalEquals("10px / 100%", dim(10, Unit.PX));
    h.evalEquals("100em / 50%", dim(200, Unit.EM));
    
    // adding/subtracting percentages
    h.evalEquals("10px + 10%", dim(11, Unit.PX));
    h.evalEquals("20px - 10%", dim(18, Unit.PX));
    h.evalEquals("100px / 100%", dim(100, Unit.PX));
    h.evalEquals("100px / 200%", dim(50, Unit.PX));
    h.evalEquals("100px / 50%", dim(200, Unit.PX));
    
    h.evalEquals("50% + 10%", dim(60, Unit.PERCENTAGE));
    h.evalEquals("50% - 10%", dim(40, Unit.PERCENTAGE));
    h.evalEquals("100% * 50%", dim(50, Unit.PERCENTAGE));
    h.evalEquals("20% * 50%", dim(10, Unit.PERCENTAGE));
    h.evalEquals("100% / 10%", dim(1000, Unit.PERCENTAGE));

    h.evalEquals("30% + 10", dim(40, Unit.PERCENTAGE));
    h.evalEquals("30% - 10", dim(20, Unit.PERCENTAGE));
    h.evalEquals("30% / 10", dim(3, Unit.PERCENTAGE));

    // Can't divide percentages by dimensions with units
    h.evalFails("10% + 10px", PERCENT_MATH_ORDER);
    h.evalFails("10% - 10px", PERCENT_MATH_ORDER);
    h.evalFails("10% / 10px", PERCENT_MATH_ORDER);
  }
 
  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.ADDITION);
    
    h.parseEquals("1 + -2", oper(ADD, dim(1), dim(-2)));
    h.parseEquals("1 - -2", oper(SUBTRACT, dim(1), dim(-2)));
    h.parseEquals("-@foo", oper(MULTIPLY, var("@foo"), dim(-1)));
    h.parseEquals("3.14 * 3.14", oper(MULTIPLY, dim(3.14), dim(3.14)));
    h.parseEquals("17 / 3", oper(DIVIDE, dim(17), dim(3)));
    h.parseEquals("3 * 4 - 5", oper(SUBTRACT, oper(MULTIPLY, dim(3), dim(4)), dim(5)));
    h.parseEquals("3 * (4 - 5)", oper(MULTIPLY, dim(3), oper(SUBTRACT, dim(4), dim(5))));
    h.parseEquals("(((1 - 2)))", oper(SUBTRACT, dim(1), dim(2)));
  }
  
  @Test
  public void testBadColorMath() throws LessException {
    String[] strings = new String[] {
        "#000 + 1px", "12em + #fff", "2 / #010101", "2 - #010101"
        };

    LessHarness h = new LessHarness(Parselets.ADDITION);

    // XXX: change to use evalFail
    for (String str : strings) {
      try {
        h.evaluate(str);
        fail("Expected operation to raise an exception: " + str);
      } catch (LessException e) {
      }
    }
  }
  
  @Test
  public void testBadMath() throws LessException {
    String[] strings = new String[] {
        "foo + 1",
        "1 * foo"
    };
    
    LessHarness h = new LessHarness(Parselets.ADDITION);

    // XXX: change to use evalFail
    for (String str : strings) {
      try {
        h.evaluate(str);
        fail("Expected operation to raise an exception: " + str);
      } catch (LessException e) {
      }
    }    
  }
  
}
