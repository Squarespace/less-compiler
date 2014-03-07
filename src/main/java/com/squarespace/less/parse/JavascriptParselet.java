package com.squarespace.less.parse;

import static com.squarespace.less.core.SyntaxErrorMaker.javascriptDisabled;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;


/**
 * For now, just detect JavaScript syntax so we can raise an error.
 */
public class JavascriptParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    int pos = 0;
    if (stm.peek() == Chars.TILDE) {
      pos++;
    }
    if (stm.peek(pos) == Chars.GRAVE_ACCENT) {
      throw stm.parseError(new LessException(javascriptDisabled()));
    }
    return null;
  }

}
