package com.squarespace.less.parse;

import com.squarespace.less.LessException;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.model.Node;


public class UrlParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    if (!CharClass.callStart(stm.peek()) || !stm.matchCallName()) {
      return null;
    }
    String name = stm.token();
    String nameLC = name.toLowerCase();
    if (nameLC.equals("url")) {
      Node result = FunctionCallParselet.parseUrl(stm);
      if (result != null) {
        return result;
      }
    }

    stm.restore(mark);
    return null;
  }
  
}
