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

import com.squarespace.less.core.Buffer;


public class Stylesheet extends BlockNode {

  public Stylesheet() {
    super();
  }

  public Stylesheet(Block block) {
    super(block);
  }

  public Stylesheet copy() {
    Stylesheet result = new Stylesheet(block().copy());
    result.charOffset = charOffset;
    result.lineOffset = lineOffset;
    return result;
  }

  @Override
  public NodeType type() {
    return NodeType.STYLESHEET;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Stylesheet) && super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public void repr(Buffer buf) {
    block.repr(buf);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    super.modelRepr(buf);
    buf.decrIndent();
  }

}
