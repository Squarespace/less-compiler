package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Variable;


public class VariableCurlyParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    boolean indirect = false;
    int pos = 0;
    if (stm.peek() != Chars.AT_SIGN) {
      return null;
    }
    pos++;
    
    if (stm.peek(pos) == Chars.AT_SIGN) {
      indirect = true;
      pos++;
    }

    if (stm.peek(pos) != Chars.LEFT_CURLY_BRACKET) {
      return null;
    }
    pos++;
    
    Mark mark = stm.mark();
    stm.seek(pos);
    if (!stm.matchIdentifier()) {
      stm.restore(mark);
      return null;
    }
    String token = '@' + stm.token();
    if (!stm.seekIf(Chars.RIGHT_CURLY_BRACKET)) {
      stm.restore(mark);
      return null;
    }
    return new Variable(indirect ? '@' + token : token, true);
  }
  
}
