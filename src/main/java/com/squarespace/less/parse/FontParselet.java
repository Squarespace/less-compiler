package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.EXPRESSION;
import static com.squarespace.less.parse.Parselets.FONT_SUB;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;


/**
 * Parse a special value for a 'font' rule.
 */
public class FontParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    List<Node> expn = new ArrayList<>(2);
    Node node = null;
    while ((node = stm.parse(FONT_SUB)) != null) {
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
