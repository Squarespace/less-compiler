package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.ENTITY;
import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;
import static com.squarespace.v6.template.less.parse.Parselets.SHORTHAND;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Expression;
import com.squarespace.v6.template.less.model.ExpressionList;
import com.squarespace.v6.template.less.model.Node;


public class FontParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    List<Node> expn = new ArrayList<>(2);
    Node node = null;
    while ((node = stm.parse(SHORTHAND, ENTITY)) != null) {
      expn.add(node);
    }
    
    ExpressionList value = new ExpressionList(new Expression(expn));
    stm.skipWs();
    if (stm.seekIf(Chars.COMMA)) {
      while ((node = stm.parse(EXPRESSION)) != null) {
        value.add(node);
        stm.skipWs();
        if (!stm.seekIf(Chars.COMMA)) { 
          break;
        }
      }
    }
    return value;
  }
  
}
