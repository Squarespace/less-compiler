package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.EXPRESSION;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;


/**
 * Parse a comma-delimited list of expressions.
 */
public class ExpressionListParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = stm.parse(EXPRESSION);
    if (node == null) {
      return null;
    }

    List<Node> expressions = new ArrayList<>();
    while (node != null) {
      expressions.add(node);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
      node = stm.parse(EXPRESSION);
    }
    return new ExpressionList(expressions);
  }

}
