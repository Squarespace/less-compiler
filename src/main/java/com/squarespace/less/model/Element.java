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

import com.squarespace.less.core.LessInternalException;


public abstract class Element extends BaseNode {

  protected final Combinator combinator;

  public Element(Combinator comb) {
    this.combinator = comb;
  }

  public Combinator combinator() {
    return combinator;
  }

  public abstract boolean isWildcard();

  @Override
  public boolean equals(Object obj) {
    throw new LessInternalException("Element subclass must implement equals(Object)!");
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.ELEMENT;
  }

}
