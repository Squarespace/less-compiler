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

import java.util.List;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.core.LessUtils;


/**
 * Represents an attribute pattern match element.
 *
 * Example: {@code .a[href~='squarespace'] }
 */
public class AttributeElement extends Element {

  /**
   * Parts of the attribute element.
   */
  protected List<Node> parts;

  /**
   * Constructs an attribute element with the given combinator.
   */
  public AttributeElement(Combinator comb) {
    super(comb);
  }

  /**
   * Returns the parts of the attribute element.
   */
  public List<Node> parts() {
    return LessUtils.safeList(parts);
  }

  /**
   * Adds a part to the attribute element.
   */
  public void add(Node part) {
    if (part == null) {
      throw new LessInternalException("Serious error: part cannot be null.");
    }
    parts = LessUtils.initList(parts, 2);
    parts.add(part);
  }

  /**
   * See {@link Element#isWildcard()}
   */
  @Override
  public boolean isWildcard() {
    return false;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append('[');
    int size = parts.size();
    for (int i = 0; i < size; i++) {
      Node part = parts.get(i);
      part.repr(buf);
    }
    buf.append(']');
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, parts);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AttributeElement) {
      AttributeElement other = (AttributeElement)obj;
      return combinator == other.combinator && safeEquals(parts, ((AttributeElement)obj).parts);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
