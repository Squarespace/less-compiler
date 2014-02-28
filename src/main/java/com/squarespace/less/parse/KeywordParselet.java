package com.squarespace.less.parse;

import com.squarespace.less.core.CharClass;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;


public class KeywordParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (!CharClass.keywordStart(stm.peek()) || !stm.matchKeyword()) {
      return null;
    }
    
    String token = stm.token();
    RGBColor color = RGBColor.fromName(token);
    if (color != null) {
      return color;
    }
    return new Keyword(stm.token());
  }
  
}
