package com.squarespace.less.parse;

import com.squarespace.less.core.CharClass;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Unit;


/**
 * Parse numbers with optional units.
 */
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
    
    return new Dimension(Double.parseDouble(value), unit);
  }
  
}
