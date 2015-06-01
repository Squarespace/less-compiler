/**
 * Copyright, 2015, Squarespace, Inc.
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
 * Represents a combinator within a selector chain.
 */
public class Combinator extends SelectorPart {

  private final CombinatorType combinatorType;

  public Combinator(CombinatorType combinatorType) {
    this.combinatorType = combinatorType;
  }

  /**
   * Indicates the combinator type for this part of the selector.
   */
  public CombinatorType combinatorType() {
    return combinatorType;
  }

  @Override
  public NodeType type() {
    return NodeType.COMBINATOR;
  }

  /**
   * @see Node#repr(Buffer)
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(combinatorType.repr());
  }

  /**
   * @see Node#modelRepr(Buffer)
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(combinatorType.fullName());
    buf.append(" '").append(combinatorType.repr()).append('\'');
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Combinator) {
      return safeEquals(combinatorType, ((Combinator)obj).combinatorType);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      return buildHashCode(combinatorType == null ? '_' : combinatorType.repr());
    }
    return hashCode;
  }
}
