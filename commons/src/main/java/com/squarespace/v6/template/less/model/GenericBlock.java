package com.squarespace.v6.template.less.model;


/**
 * General purpose block.
 */
public class GenericBlock extends BlockNode {

  public GenericBlock(Block block) {
    super(block);
  }
  
  @Override
  public NodeType type() {
    return NodeType.BINDINGS;
  }
  
}
