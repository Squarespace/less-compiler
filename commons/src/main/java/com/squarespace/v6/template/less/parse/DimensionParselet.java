package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Unit;


public class DimensionParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) {
    if (!CharClass.dimensionStart(stm.peek()) || !stm.matchDimensionValue()) {
      return null;
    }
    String value = stm.token();

    Unit unit = null;
    if (stm.matchDimensionUnit()) {
      unit = Unit.get(stm.token());
    }
    
    if (value.indexOf('.') != -1) {
      return new Dimension(Double.parseDouble(value), unit);
    } 
    return new Dimension(Long.parseLong(value), unit);
  }
  
}
