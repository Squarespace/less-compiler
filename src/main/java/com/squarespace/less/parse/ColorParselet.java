package com.squarespace.less.parse;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.RGBColor;


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
