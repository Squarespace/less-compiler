package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.KEYWORD;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;


/**
 * Only parses keywords which map to color values.
 */
public class ColorKeywordParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    Node node = stm.parse(KEYWORD);
    if (node != null && node.is(NodeType.COLOR)) {
      return node;
    }
    stm.restore(mark);
    return null;
  }

}
