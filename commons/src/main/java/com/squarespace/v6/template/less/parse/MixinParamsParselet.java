package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.SyntaxErrorType.EXPECTED_MISC;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.parse.Parselets.COMMENT;
import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;
import static com.squarespace.v6.template.less.parse.Parselets.KEYWORD;
import static com.squarespace.v6.template.less.parse.Parselets.LITERAL;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Parameter;
import com.squarespace.v6.template.less.model.Variable;


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
    Node temp = stm.parse(VARIABLE, LITERAL, KEYWORD);
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
        // XXX: error code, position.
        throw new LessException(error(EXPECTED_MISC).arg0("expression").arg1(stm.remainder()));
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
