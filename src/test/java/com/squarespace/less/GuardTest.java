package com.squarespace.less;

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.Constants.TRUE;
import static com.squarespace.less.model.Operator.AND;
import static com.squarespace.less.model.Operator.EQUAL;
import static com.squarespace.less.model.Operator.NOT_EQUAL;

import org.testng.annotations.Test;

import com.squarespace.less.core.Constants;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.parse.Parselets;


public class GuardTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    Dimension one = dim(1);
    Dimension two = dim(2);
    Condition oneEq1 = cond(EQUAL, one, one);
    Condition twoEq2 = cond(EQUAL, two, two);
    Condition oneNe2 = cond(NOT_EQUAL, one, two);

    LessHarness h = new LessHarness(Parselets.GUARD);

    h.parseEquals("when (1)",
        guard(cond(EQUAL, one, Constants.TRUE)));

    h.parseEquals("when (1=1), (2=2) and (1!=2)",
        guard(oneEq1, cond(AND, twoEq2, oneNe2)));

    h.parseEquals("when not (1=1)",
        guard(cond(EQUAL, one, one, true)));

    h.parseEquals("when (1=1) and not (2=2)",
        guard(cond(AND, oneEq1, cond(EQUAL, two, two, true))));

    h.parseEquals("when (true), (false)",
        guard(cond(EQUAL, TRUE, TRUE), cond(EQUAL, FALSE, TRUE)));

    h.parseEquals("when(true),(false)",
        guard(cond(EQUAL, TRUE, TRUE), cond(EQUAL, FALSE, TRUE)));
  }

}
