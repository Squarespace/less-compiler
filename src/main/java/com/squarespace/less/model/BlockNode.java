/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.nio.file.Path;

import com.squarespace.less.core.Buffer;


/**
 * Base class for all nodes that have nested blocks.
 */
public abstract class BlockNode extends BaseNode implements BlockLike {

  /**
   * Nested block associated with this node.
   */
  protected Block block;

  /**
   * Original instance that was parsed from the document. We hold a reference
   * to it for evaluation purposes.
   */
  protected BlockNode originalBlockNode;

  /**
   * Indicates whether this block was marked important.
   */
  protected boolean important;

  /**
   * Path to the file in which this node was defined.
   */
  protected Path fileName;

  /**
   * Constructs a block node with an empty block.
   */
  public BlockNode() {
    this(new Block());
  }

  /**
   * Constructs a block node with the associated block.
   */
  public BlockNode(Block block) {
    this.block = block;
    this.originalBlockNode = this;
  }

  /**
   * Returns the block.
   */
  public Block block() {
    return block;
  }

  /**
   * Append the contents of the block argument to our block.
   */
  public void append(Block block) {
    this.block.appendBlock(block);
  }

  /**
   * Returns the original (parsed) instance of this block node. Nodes can be copied
   * during evaluation, so this provides a reference to the original instance from
   * which the current instance was derived.
   */
  public BlockNode original() {
    return originalBlockNode;
  }

  /**
   * Marks this as the original (parsed) instance.
   */
  public void markOriginal() {
    originalBlockNode = this;
  }

  /**
   * Indicates whether this node is marked important.
   */
  public boolean important() {
    return important;
  }

  /**
   * Marks this node as important.
   */
  public void markImportant() {
    important = true;
  }

  /**
   * Adds a node to the block.
   */
  public void add(Node node) {
    block.appendNode(node);
  }

  /**
   * Sets the nested block.
   */
  public void setBlock(Block block) {
    this.block = block;
  }

  /**
   * Returns the path to the file in which this node was defined.
   */
  public Path fileName() {
    return fileName;
  }

  /**
   * Sets the path to the file in which this node was defined.
   */
  public void fileName(Path fileName) {
    this.fileName = fileName;
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    if (block != null) {
      block.modelRepr(buf);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BlockNode) {
      BlockNode other = (BlockNode)obj;
      return important == other.important
          && safeEquals(block, ((BlockNode)obj).block);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
