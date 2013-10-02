package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.COMMENT_RULE;
import static com.squarespace.v6.template.less.parse.Parselets.DIRECTIVE;
import static com.squarespace.v6.template.less.parse.Parselets.MIXIN;
import static com.squarespace.v6.template.less.parse.Parselets.MIXIN_CALL;
import static com.squarespace.v6.template.less.parse.Parselets.RULE;
import static com.squarespace.v6.template.less.parse.Parselets.RULESET;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Node;


public class PrimaryParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Block block = new Block();
    Node node = null;
    skipEmpty(stm);
    while ((node = stm.parse(MIXIN, RULE, RULESET, COMMENT_RULE, MIXIN_CALL, DIRECTIVE)) != null) {
      block.append(node);
      skipEmpty(stm);
    }
    skipEmpty(stm);
    return block;
  }

  private void skipEmpty(LessStream stm) {
    while (CharClass.skippable(stm.peek())) {
      stm.seek1();
    }
  }

}
