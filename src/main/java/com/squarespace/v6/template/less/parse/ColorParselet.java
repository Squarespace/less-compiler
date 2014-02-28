package com.squarespace.v6.template.less.parse;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.RGBColor;


/**
 * Parse a 3- or 6-segment hexadecimal color value.
 */
public class ColorParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (stm.peek() == Chars.NUMBER_SIGN && stm.matchHexColor()) {
      return RGBColor.fromHex(stm.token());
    }
    return null;
  }
  
}
