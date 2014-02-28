package com.squarespace.less.parse;

import static com.squarespace.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.EXPRESSION;
import static com.squarespace.less.parse.Parselets.MIXIN_PARAMETER;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Parameter;
import com.squarespace.less.model.Variable;


public class MixinParamsParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      stm.restore(mark);
      return null;
    }

    MixinParams params = new MixinParams();
    do {
      stm.parse(COMMENT);
      if (matchVariadic(stm)) {
        stm.seek(3);
        params.add(new Parameter(null, true));
        break;
      }
      
      Parameter param = parseParam(stm);
      if (param == null) {
        break;
      } 
      params.add(param);

      stm.skipWs();
      char ch = stm.peek();
      if (ch != Chars.COMMA && ch != Chars.SEMICOLON) {
        break;
      }
      stm.seek1();
      
    } while (true);

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      stm.restore(mark);
      return null;
    }
    return params;
  }
  
  private Parameter parseParam(LessStream stm) throws LessException {
    Node temp = stm.parse(MIXIN_PARAMETER);
    if (temp == null) {
      return null;
    }
    if (!temp.is(NodeType.VARIABLE)) {
      return new Parameter(null, temp);
    }
    
    Variable var = (Variable)temp;
    
    stm.skipWs();
    if (stm.seekIf(Chars.COLON)) {
      stm.skipWs();
      Node value = stm.parse(EXPRESSION);
      if (value == null) {
        throw stm.parseError(new LessException(expected("an expression")));
      }
      return new Parameter(var.name(), value);
      
    } else if (matchVariadic(stm)) {
      stm.seek(3);
      return new Parameter(var.name(), true);
    }

    return new Parameter(var.name());
  }

  private boolean matchVariadic(LessStream stm) throws LessException {
    return stm.peek() == Chars.PERIOD && stm.peek(1) == Chars.PERIOD && stm.peek(2) == Chars.PERIOD;
  }
  
}
