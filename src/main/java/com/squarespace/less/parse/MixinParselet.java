package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.BLOCK;
import static com.squarespace.less.parse.Parselets.COMMENT;
import static com.squarespace.less.parse.Parselets.GUARD;
import static com.squarespace.less.parse.Parselets.MIXIN_PARAMS;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;


public class MixinParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    char ch = stm.peek();
    if (ch != Chars.PERIOD && ch != Chars.NUMBER_SIGN) {
      return null;
    }
    Mark mark = stm.mark();
    if (!stm.matchMixinName()) {
      return null;
    }
    
    String name = stm.token();
    MixinParams params = (MixinParams)stm.parse(MIXIN_PARAMS);
    if (params == null) {
      stm.restore(mark);
      return null;
    }
    stm.parse(COMMENT);

    Guard guard = (Guard)stm.parse(GUARD);
    stm.parse(COMMENT);

    Node block = stm.parse(BLOCK);
    if (block == null) {
      stm.restore(mark);
      return null;
    }
    
    Mixin mixin = new Mixin(name, params, guard, (Block)block);
    mixin.markOriginal();
    return mixin;
  }

}
