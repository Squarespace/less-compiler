package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.SyntaxErrorType.EXPECTED_MISC;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.model.Operator.AND;
import static com.squarespace.v6.template.less.model.Operator.EQUAL;
import static com.squarespace.v6.template.less.parse.Parselets.ADDITION;
import static com.squarespace.v6.template.less.parse.Parselets.KEYWORD;
import static com.squarespace.v6.template.less.parse.Parselets.QUOTED;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.core.Constants;
import com.squarespace.v6.template.less.model.Condition;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Operator;


public class ConditionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Condition cond = parseCondition(stm);
    stm.skipWs();
    while (stm.matchAnd()) {
      Condition sub = parseCondition(stm);
      cond = new Condition(AND, cond, sub, false);
      stm.skipWs();
    }
    return cond;
  }
  
  private Condition parseCondition(LessStream stm) throws LessException {
    stm.skipWs();
    boolean negate = stm.matchNot();
    
    Condition res = null;
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      throw new LessException(error(EXPECTED_MISC).arg0('(').arg1(stm.remainder()));
    }
    
    Node left = parseValue(stm);
    if (left == null) {
      throw new LessException(error(EXPECTED_MISC).arg0("condition value").arg1(stm.remainder()));
    }
    
    stm.skipWs();
    if (stm.matchBoolOperator()) {
      Operator op = Operator.fromString(stm.token());
      Node right = parseValue(stm);
      if (right != null) {
        res = new Condition(op, left, right, negate);

      } else {
        throw new LessException(error(EXPECTED_MISC).arg0("expression").arg1(stm.remainder()));
      }
    } else {
      res = new Condition(EQUAL, left, Constants.TRUE, negate);
    }

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw new LessException(error(EXPECTED_MISC).arg0(')').arg1(stm.remainder()));
    }
    return res;
  }

  
  private Node parseValue(LessStream stm) throws LessException {
    return stm.parse(ADDITION, KEYWORD, QUOTED);
  }
  
}
