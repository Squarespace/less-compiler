package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Alpha;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.Node;


public class AlphaParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!stm.matchOpacity()) {
      return null;
    }
    
    // Look for either a numeric value or variable.
    Node value = null;
    if (stm.matchDigits()) {
      value = new Anonymous(stm.token());
      
    } else {
      value = stm.parse(VARIABLE);
    }

    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw stm.parseError(new LessException(expected("right parenthesis ')' to end alpha")));
    }
    return new Alpha(value == null ? new Anonymous("") : value);
  }
  
}
