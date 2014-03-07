package com.squarespace.less.model;


/**
 * General purpose block.
 */
public class GenericBlock extends BlockNode {

  public GenericBlock(Block block) {
    super(block);
  }

  @Override
  public NodeType type() {
    return NodeType.GENERIC_BLOCK;
  }

}
