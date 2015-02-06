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

import com.squarespace.less.core.Buffer;


/**
 * A CSS directive that has an associated block.
 *
 * Example:
 * <pre>
 *     {@literal @}-webkit-keyframes frames {
 *         ...
 *     }
 * </pre>
 */
public class BlockDirective extends BlockNode {

  /**
   * Name of the block directive.
   */
  protected final String name;

  /**
   * Constructs a block directive with the given name and block.
   */
  public BlockDirective(String name, Block block) {
    this.name = name;
    setBlock(block);
  }

  /**
   * Returns the name of the block directive.
   */
  public String name() {
    return name;
  }

  /**
   * Copies the block directive.
   */
  public BlockDirective copy() {
    BlockDirective result = new BlockDirective(name, block.copy());
    result.copyBase(this);
    result.fileName = fileName;
    return result;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.BLOCK_DIRECTIVE;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(name);
    if (buf.compress()) {
      buf.append('{');
    } else {
      buf.append(" {\n");
    }
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    if (buf.compress()) {
      buf.append('}');
    } else {
      buf.indent().append("}\n");
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent().indent();
    super.modelRepr(buf);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BlockDirective) {
      BlockDirective other = (BlockDirective)obj;
      return safeEquals(name, other.name) && super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
