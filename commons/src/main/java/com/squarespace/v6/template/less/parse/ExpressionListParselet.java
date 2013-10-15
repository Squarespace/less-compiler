package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.ExpressionList;
import com.squarespace.v6.template.less.model.Node;


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
