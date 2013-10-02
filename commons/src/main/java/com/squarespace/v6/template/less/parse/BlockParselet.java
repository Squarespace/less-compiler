package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.PRIMARY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Node;


public class BlockParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    stm.skipWs();
    if (stm.peek() != Chars.LEFT_CURLY_BRACKET) {
      return null;
    }
    
    Mark mark = stm.mark();
    stm.seek1();
    Block block = (Block)stm.parse(PRIMARY);

    skipEmpty(stm);
    if (stm.peek() != Chars.RIGHT_CURLY_BRACKET) {
      stm.restore(mark);
      return null;
    }
    stm.seek1();
    return block == null ? new Block() : block;
  }

  private void skipEmpty(LessStream stm) {
    while (CharClass.skippable(stm.peek())) {
      stm.seek1();
    }
  }

}
