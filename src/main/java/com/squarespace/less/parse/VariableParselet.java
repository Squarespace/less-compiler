package com.squarespace.less.parse;

import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Variable;


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
