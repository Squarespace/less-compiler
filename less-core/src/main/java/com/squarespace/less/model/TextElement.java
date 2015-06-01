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
 * Represents an element or pseudo-element, not including
 * attribute selector syntax.
 */
public class TextElement extends SelectorPart {

  /**
   * Name of the element.
   */
  protected final String name;

  /**
   * Constructs a text element.
   */
  public TextElement(String name) {
    this.name = name;
  }

  /**
   * Returns the text element's name.
   */
  public String name() {
    return name;
  }

  @Override
  public NodeType type() {
    return NodeType.TEXT_ELEMENT;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    if (name != null) {
      buf.append(name);
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TextElement) {
      return safeEquals(name, ((TextElement)obj).name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hashCode == 0 ? buildHashCode(name) : hashCode;
  }

}
