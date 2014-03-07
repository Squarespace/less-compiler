package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.PRIMARY;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;


/**
 * Handles parsing of '{' PRIMARY '}' sequence for all block types (ruleset, media, etc).
 */
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
    stm.skipEmpty();

    if (stm.peek() != Chars.RIGHT_CURLY_BRACKET) {
      stm.restore(mark);
      return null;
    }
    stm.seek1();
    return block;
  }

}
