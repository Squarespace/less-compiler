package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Node;


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
