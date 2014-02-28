package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.PRIMARY;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Stylesheet;


public class StylesheetParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node block = stm.parse(PRIMARY);
    Stylesheet sheet = new Stylesheet((Block)block);

    stm.checkComplete();
    return sheet;
  }
  
}
