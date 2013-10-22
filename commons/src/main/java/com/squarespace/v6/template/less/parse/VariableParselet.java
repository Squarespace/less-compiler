package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Variable;


/**
 * Parses direct and indirect variable references.
 */
public class VariableParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    Mark mark = stm.mark();
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

    stm.seek(pos);
    if (!stm.matchIdentifier()) {
      stm.restore(mark);
      return null;
    }
    String name = '@' + stm.token();
    return new Variable(indirect ? '@' + name : name);
  }

}
