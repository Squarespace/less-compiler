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


public class BlockDirective extends BlockNode {

  private final String name;

  public BlockDirective(String name, Block block) {
    this.name = name;
    setBlock(block);
  }

  public String name() {
    return name;
  }

  public BlockDirective copy() {
    BlockDirective result = new BlockDirective(name, block.copy());
    result.fileName = fileName;
    return result;
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

  @Override
  public NodeType type() {
    return NodeType.BLOCK_DIRECTIVE;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(name);
    buf.append(" {\n");
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    buf.indent().append("}\n");
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent().indent();
    super.modelRepr(buf);
    buf.decrIndent();
  }

}
