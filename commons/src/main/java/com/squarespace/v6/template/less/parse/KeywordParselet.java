package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Keyword;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.RGBColor;


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
