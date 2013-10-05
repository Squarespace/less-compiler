package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.core.SyntaxErrorMaker.javascriptDisabled;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;


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
      throw new LessException(javascriptDisabled());
    }
    return null;
  }
  
}
