package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.ADDITION;
import static com.squarespace.v6.template.less.parse.Parselets.COMMENT;
import static com.squarespace.v6.template.less.parse.Parselets.ENTITY;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Expression;
import com.squarespace.v6.template.less.model.Node;


/**
 * Parse a space-delimited list of entities.
 */
public class ExpressionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = stm.parse(COMMENT, ADDITION, ENTITY);
    if (node == null) {
      return null;
    }
    
    List<Node> entities = new ArrayList<>();
    while (node != null) {
      entities.add(node);
      node = stm.parse(COMMENT, ADDITION, ENTITY);
    }
    return entities.size() == 1 ? entities.get(0) : new Expression(entities);
  }
  
}
