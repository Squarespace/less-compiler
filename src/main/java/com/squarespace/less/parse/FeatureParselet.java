package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.ENTITY;
import static com.squarespace.less.parse.Parselets.KEYWORD;
import static com.squarespace.less.parse.Parselets.PROPERTY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.Rule;


/**
 * Parse a list of expressions which are part of a MEDIA or IMPORT node's features clause.
 */
public class FeatureParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node elem = parseOne(stm);
    if (elem == null) {
      return null;
    }

    Expression expn = new Expression();
    while (elem != null) {
      expn.add(elem);
      elem = parseOne(stm);
    }
    return expn;
  }

  private Node parseOne(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    Node node = stm.parse(KEYWORD);
    if (node != null) {
      return node;

    } else if (stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      Node prop = parseProperty(stm);
      node = stm.parse(ENTITY);
      stm.skipWs();
      if (stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
        if (prop != null && node != null) {
          return new Paren(new Rule(prop, node));

        } else if (node != null) {
          return new Paren(node);
        }
      }
    }

    stm.restore(mark);
    return null;
  }

  private Property parseProperty(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    Node prop = stm.parse(PROPERTY);
    if (prop != null) {
      stm.skipWs();
      if (stm.seekIf(Chars.COLON)) {
        return (Property)prop;
      }
    }
    stm.restore(mark);
    return null;
  }

}
