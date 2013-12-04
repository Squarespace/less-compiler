package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.core.SyntaxErrorMaker.alphaUnitsInvalid;
import static com.squarespace.v6.template.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.v6.template.less.parse.Parselets.ALPHA_SUB;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Alpha;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;


public class AlphaParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.matchOpacity()) {
      return null;
    }
    
    Node value = stm.parse(ALPHA_SUB);
    if (value != null && value.is(NodeType.DIMENSION)) {
      Dimension dim = (Dimension)value;
      if (dim.unit() != null) {
        throw stm.parseError(new LessException(alphaUnitsInvalid(dim)));
      }
    }
    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      if (value == null) {
        throw stm.parseError(new LessException(expected("expected a unit-less number or variable for alpha")));
      } else {
        throw stm.parseError(new LessException(expected("right parenthesis ')' to end alpha")));
      }
    }
    return new Alpha(value == null ? new Anonymous("") : value);
  }
  
}
