package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.COMMENT_RULE;
import static com.squarespace.v6.template.less.parse.Parselets.DIRECTIVE;
import static com.squarespace.v6.template.less.parse.Parselets.MIXIN;
import static com.squarespace.v6.template.less.parse.Parselets.MIXIN_CALL;
import static com.squarespace.v6.template.less.parse.Parselets.RULE;
import static com.squarespace.v6.template.less.parse.Parselets.RULESET;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.Node;


public class PrimaryParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Block block = new Block();
    Node node = null;
    stm.skipEmpty();
    
    // Save current stream position before parsing each primary rule type.
    Mark position = stm.mark();
    while ((node = stm.parse(MIXIN, RULE, RULESET, COMMENT_RULE, MIXIN_CALL, DIRECTIVE)) != null) {
      // Assign stream position to successfully-parsed rule.
      node.setLineOffset(position.lineOffset);
      node.setCharOffset(position.charOffset);
      block.appendNode(node);
      stm.skipEmpty();
      stm.mark(position);
    }
    stm.skipEmpty();
    return block;
  }

}
