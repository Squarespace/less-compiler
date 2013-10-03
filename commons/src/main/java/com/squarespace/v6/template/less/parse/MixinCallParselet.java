package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.MIXIN_CALL_ARGS;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.Combinator;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.TextElement;


public class MixinCallParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    char ch = stm.peek();
    if (ch != Chars.PERIOD && ch != Chars.NUMBER_SIGN) {
      return null;
    }
    Mark mark = stm.mark();
    Selector selector = new Selector();
    Combinator comb = null;
    while (stm.matchMixinName()) {
      selector.add(new TextElement(comb, stm.token()));
      
      int skipped = stm.skipWs();
      if (stm.peek() == Chars.GREATER_THAN_SIGN) {
        comb = Combinator.CHILD;
        stm.seek1();
      } else if (skipped > 0) {
        comb = Combinator.DESC;
      } else {
        comb = null;
      }
      stm.skipWs();
    }
    
    // If we failed to parse a valid selector, reset and bail out.
    if (selector.isEmpty()) {
      stm.restore(mark);
      return null;
    }
    
    MixinCallArgs args = (MixinCallArgs) stm.parse(MIXIN_CALL_ARGS);
    stm.skipWs();
    boolean important = stm.matchImportant();
    
    stm.skipWs();
    ch = stm.peek();
    if (ch == Chars.SEMICOLON) {
      stm.seek1();
      return new MixinCall(selector, args, important);

    } else if (ch == Chars.RIGHT_CURLY_BRACKET || ch == Chars.EOF) {
      return new MixinCall(selector, args, important);
    }
    
    stm.restore(mark);
    return null;
  }

  
}
