package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.PRIMARY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.core.ErrorMaker;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Stylesheet;


public class StylesheetParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node block = stm.parse(PRIMARY);
    Stylesheet sheet = new Stylesheet((Block)block);

    // Any input left?  We should have a complete parse
    stm.skipWs();
    if (stm.peek() != Chars.EOF) {
      LessException exc = new LessException(ErrorMaker.incompleteParse());
      ParseUtils.parseError(exc, stm.raw, stm.furthest);
      throw exc;
    }
    return sheet;
  }
  
}
