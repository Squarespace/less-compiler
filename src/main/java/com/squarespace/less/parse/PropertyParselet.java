package com.squarespace.less.parse;

import com.squarespace.less.core.CharClass;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Property;


public class PropertyParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (!CharClass.propertyStart(stm.peek()) || !stm.matchProperty()) {
      return null;
    }
    return new Property(stm.token());
  }

}
