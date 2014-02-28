package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.BLOCK;
import static com.squarespace.less.parse.Parselets.SELECTORS;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selectors;


public class RulesetParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    Selectors group = (Selectors) stm.parse(SELECTORS);
    if (group == null) {
      return null;
    }
    
    Node block = stm.parse(BLOCK);
    if (block == null) {
      stm.restore(mark);
      return null;
    }
    
    Ruleset ruleset = new Ruleset(group, (Block)block);
    ruleset.fileName(stm.fileName());
    return ruleset;
  }
  
}
