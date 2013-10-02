package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.BLOCK;
import static com.squarespace.v6.template.less.parse.Parselets.SELECTORS;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selectors;


public class RulesetParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    Selectors group = (Selectors) stm.parse(SELECTORS);
    if (group == null) {
      return null;
    }
    
//    stm.push(stm.raw().substring(mark.index, stm.position()));
    Node block = stm.parse(BLOCK);
    if (block == null) {
      stm.restore(mark);
      return null;
    }
    
    return new Ruleset(group, (Block)block);
  }
  
}
