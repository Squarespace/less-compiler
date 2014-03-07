package com.squarespace.less;

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.Constants.TRUE;
import static com.squarespace.less.model.Operator.AND;
import static com.squarespace.less.model.Operator.EQUAL;
import static com.squarespace.less.model.Operator.GREATER_THAN;
import static com.squarespace.less.model.Operator.GREATER_THAN_OR_EQUAL;
import static com.squarespace.less.model.Operator.LESS_THAN;
import static com.squarespace.less.model.Operator.LESS_THAN_OR_EQUAL;
import static com.squarespace.less.model.Operator.NOT_EQUAL;
import static com.squarespace.less.model.Operator.OR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.Constants;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.True;
import com.squarespace.less.model.Unit;
import com.squarespace.less.parse.Parselets;


public class ConditionTest extends LessTestBase {

  @Test
  public void testEquals() {
    Dimension one = dim(1);
    Dimension two = dim(2);

    assertEquals(cond(EQUAL, one, one), cond(EQUAL, one, one));
    assertEquals(cond(EQUAL, one, one, true), cond(EQUAL, one, one, true));

    assertNotEquals(cond(EQUAL, one, one), cond(EQUAL, one, two));
    assertNotEquals(cond(EQUAL, one, one), cond(NOT_EQUAL, one, one));
    assertNotEquals(cond(EQUAL, one, one, false), cond(EQUAL, one, one, true));
  }

  @Test
  public void testModelReprSafety() {
    cond(EQUAL, dim(1), dim(1), true).toString();
  }

  @Test
  public void testCompare() throws LessException {
    Dimension two = dim(2);
    Dimension four = dim(4);
    Dimension twoS = dim(2, Unit.S);
    Dimension twoKMS = dim(2000, Unit.MS);
    Dimension fourS = dim(4, Unit.S);
    Dimension twoPX = dim(2, Unit.PX);
    Quoted strTrue = quoted('"', false, "true");
    Quoted bareTrue = quoted('"', true, "true");

    // Booleans (only the string "true" counts)
    compare(true, cond(AND, TRUE, anon("true")));
    compare(true, cond(AND, TRUE, bareTrue));
    compare(false, cond(AND, TRUE, strTrue));
    compare(false, cond(AND, TRUE, two));

    compare(true, cond(OR, TRUE, FALSE));
    compare(true, cond(OR, FALSE, TRUE));
    compare(true, cond(OR, bareTrue, FALSE));
    compare(true, cond(OR, two, TRUE));

    // Unit-less values
    compare(true, cond(EQUAL, two, two));
    compare(true, cond(NOT_EQUAL, two, four));
    compare(false, cond(NOT_EQUAL, two, two));

    // Mixed unit and unit-less.
    compare(true, cond(EQUAL, two, twoS));
    compare(true, cond(EQUAL, twoS, two));
    compare(true, cond(NOT_EQUAL, two, fourS));
    compare(true, cond(NOT_EQUAL, two, four));

    // Mixed units requiring conversions
    compare(true, cond(EQUAL, twoS, twoKMS));
    compare(true, cond(EQUAL, twoKMS, twoS));
    compare(false, cond(EQUAL, twoS, fourS));
    compare(false, cond(EQUAL, fourS, twoS));

    compare(true, cond(NOT_EQUAL, twoS, fourS));
    compare(true, cond(NOT_EQUAL, fourS, twoS));
    compare(false, cond(NOT_EQUAL, twoS, twoKMS));

    compare(true, cond(LESS_THAN, twoS, fourS));
    compare(false, cond(LESS_THAN, twoS, twoKMS));
    compare(false, cond(LESS_THAN, fourS, twoS));

    compare(true, cond(LESS_THAN_OR_EQUAL, twoS, twoKMS));
    compare(true, cond(LESS_THAN_OR_EQUAL, twoS, fourS));
    compare(false, cond(LESS_THAN_OR_EQUAL, fourS, twoS));

    compare(true, cond(GREATER_THAN, fourS, twoS));
    compare(false, cond(GREATER_THAN, twoS, fourS));
    compare(false, cond(GREATER_THAN, twoS, twoKMS));

    compare(true, cond(GREATER_THAN_OR_EQUAL, fourS, twoS));
    compare(true, cond(GREATER_THAN_OR_EQUAL, twoS, twoKMS));
    compare(false, cond(GREATER_THAN_OR_EQUAL, twoS, fourS));

    // Incompatible units
    compare(false, cond(EQUAL, twoS, twoPX));
    compare(true, cond(NOT_EQUAL, twoS, twoPX));

    // Negation
    compare(true, cond(NOT_EQUAL, two, two, true));
    compare(false, cond(EQUAL, two, two, true));
  }

  @Test
  public void testGuard() throws LessException {
    Condition eq = cond(EQUAL, dim(2), dim(2));
    Condition ne = cond(EQUAL, dim(2), dim(4));
    compare(true, eq, eq);
    compare(true, ne, eq, ne);
    compare(false, ne, ne);
    compare(false, ne, ne, ne);
  }

  @Test
  public void testCondition() throws LessException {
    LessHarness h = new LessHarness(Parselets.CONDITION);

    h.parseEquals("(1=1)", cond(EQUAL, dim(1), dim(1)));
    h.parseEquals("(1<2)", cond(LESS_THAN, dim(1), dim(2)));
    h.parseEquals("(1>2)", cond(GREATER_THAN, dim(1), dim(2)));
    h.parseEquals("(1>=2)", cond(GREATER_THAN_OR_EQUAL, dim(1), dim(2)));
    h.parseEquals("(1=>2)", cond(GREATER_THAN_OR_EQUAL, dim(1), dim(2)));
    h.parseEquals("(1<=2)", cond(LESS_THAN_OR_EQUAL, dim(1), dim(2)));
    h.parseEquals("(1=<2)", cond(LESS_THAN_OR_EQUAL, dim(1), dim(2)));
    h.parseEquals("(1!=2)", cond(NOT_EQUAL, dim(1), dim(2)));
    h.parseEquals("(1<>2)", cond(NOT_EQUAL, dim(1), dim(2)));

    h.parseEquals("(@b=1px)", cond(EQUAL, var("@b"), dim(1, Unit.PX)));
    h.parseEquals("(@a=true)", cond(EQUAL, var("@a"), new True()));
    h.parseEquals("(1px!=2px)", cond(NOT_EQUAL, dim(1, Unit.PX), dim(2, Unit.PX)));
    h.parseEquals("('foo'<='bar')", cond(LESS_THAN_OR_EQUAL, quoted('\'', false, "foo"), quoted('\'', false, "bar")));
    h.parseEquals("(xyz<>abc)", cond(NOT_EQUAL, kwd("xyz"), kwd("abc")));
  }

  private void compare(boolean expected, Condition ... conditions) throws LessException {
    LessHarness h = new LessHarness();
    Node actual = null;
    if (conditions.length == 1) {
      actual = h.evaluate(conditions[0]);

    } else {
      Guard guard = new Guard();
      guard.addAll(conditions);
      actual = h.evaluate(guard);
    }

    if (expected) {
      assertEquals(actual, Constants.TRUE);
    } else {
      assertEquals(actual, Constants.FALSE);
    }
  }

}
