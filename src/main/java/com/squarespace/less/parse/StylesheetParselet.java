package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.PRIMARY;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;


public class StylesheetParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node block = stm.parse(PRIMARY);
    Stylesheet sheet = new Stylesheet((Block)block);

    stm.checkComplete();
    return sheet;
  }
  
}
