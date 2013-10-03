package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.SyntaxErrorType.EXPECTED_MISC;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
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
      // XXX: fixme
      throw new LessException(error(EXPECTED_MISC).arg0(')').arg1(stm.remainder()));
    }
    return new Alpha(value == null ? new Anonymous("") : value);
  }
  
}
