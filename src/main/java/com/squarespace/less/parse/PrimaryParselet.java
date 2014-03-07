package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.PRIMARY_SUB;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;


public class PrimaryParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Block block = new Block();
    Node node = null;
    stm.skipEmpty();

    // Save current stream position before parsing each primary rule type.
    Mark position = stm.mark();
    while ((node = stm.parse(PRIMARY_SUB)) != null) {
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
