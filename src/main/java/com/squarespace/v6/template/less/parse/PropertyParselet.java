package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Property;


public class PropertyParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (!CharClass.propertyStart(stm.peek()) || !stm.matchProperty()) {
      return null;
    }
    return new Property(stm.token());
  }

}
