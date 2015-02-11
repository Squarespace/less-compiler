package com.squarespace.less.parse;

import static com.squarespace.less.core.Chars.LEFT_PARENTHESIS;
import static com.squarespace.less.core.Chars.RIGHT_PARENTHESIS;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;


/**
 * Parses a detached {@link Ruleset} call.
 */
public class RulesetCallParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (stm.peek() != Chars.AT_SIGN) {
      return null;
    }

    Mark mark = stm.mark();
    stm.seek1();
    if (stm.matchIdentifier() && stm.peek() == LEFT_PARENTHESIS && stm.peek(1) == RIGHT_PARENTHESIS) {
      String name = stm.token();
      stm.seek(2);
      if (RuleParselet.end(stm)) {
        return stm.context().nodeBuilder().buildVariable("@" + name, false, true);
      }
    }
    stm.restore(mark);
    return null;
  }

}
