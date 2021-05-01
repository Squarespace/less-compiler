package com.squarespace.less.model;


/**
 * Acts like a block, storing zero or more child nodes.
 */
public interface BlockLike {

  /**
   * Type of this node.
   */
  NodeType type();

  /**
   * Add a node to this block.
   */
  void add(Node node);

  /**
   * Append the block's node to ourselves.
   */
  void append(Block block);
}
