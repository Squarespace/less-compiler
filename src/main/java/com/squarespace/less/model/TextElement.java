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
 * A textual {@link Element}.
 */
public class TextElement extends Element {

  /**
   * Name of the element.
   */
  protected final String name;

  /**
   * Flag indicating whether this is a wildcard.
   */
  protected final boolean isWildcard;

  /**
   * Constructs an empty text element.
   */
  public TextElement(Combinator comb) {
    super(comb);
    this.name = null;
    this.isWildcard = false;
  }

  /**
   * Constructs a text element.
   */
  public TextElement(Combinator comb, String name) {
    super(comb);
    this.name = name;
    this.isWildcard = name.equals("&");
  }

  /**
   * Returns the text element's name.
   */
  public String name() {
    return name;
  }

  /**
   * @see Element#copy(Combinator)
   */
  public Element copy(Combinator replacement) {
    return new TextElement(replacement, name);
  }

  /**
   * See {@link Element#isWildcard()}
   */
  @Override
  public boolean isWildcard() {
    return isWildcard;
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
    buf.append(' ');
    buf.append(combinator == null ? "<null>" : combinator.toString());
    buf.append(' ').append(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TextElement) {
      TextElement other = (TextElement)obj;
      return combinator == other.combinator && safeEquals(name, other.name);
    }
    return false;
  }

  @Override
  public boolean equalsIgnoreCombinator(Object obj) {
    if (obj instanceof TextElement) {
      return safeEquals(name, ((TextElement)obj).name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    // Hash codes do not include the combinator
    return hashCode == 0 ? buildHashCode(name) : hashCode;
  }

}
