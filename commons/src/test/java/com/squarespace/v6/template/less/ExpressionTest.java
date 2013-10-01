package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Operator.ADD;
import static com.squarespace.v6.template.less.model.Unit.PX;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.RGBColor;
import com.squarespace.v6.template.less.parse.Parselets;


public class ExpressionTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    RGBColor white = rgb(255, 255, 255, 1.0, true);
    RGBColor black = rgb(0, 0, 0, 1.0, true);
    LessHarness h = new LessHarness(Parselets.EXPRESSION);

    h.parseEquals("white black", expn(white, black));
    h.parseEquals("12px -5px", expn(dim(12, PX), dim(-5, PX)));
    h.parseEquals("1px solid black", expn(dim(1, PX), kwd("solid"), black));
    h.parseEquals("'bar' 3 + 1", expn(quoted('\'', "bar"), oper(ADD, dim(3), dim(1))));
  }
  
}
