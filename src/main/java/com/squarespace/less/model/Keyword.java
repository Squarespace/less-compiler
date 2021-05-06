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
import static com.squarespace.less.model.NodeType.KEYWORD;

import com.squarespace.less.core.Buffer;


/**
 * <p>
 * A LESS / CSS keyword string.
 * </p>
 *
 * Example: {@code border: 1px solid red} both solid and red are keywords.
 */
public class Keyword implements Node {

  /**
   * Keyword string value.
   */
  protected final String value;

  /**
   * Constructs a keyword with the given string value.
   */
  public Keyword(String value) {
    this.value = value;
  }

  /**
   * Returns the keyword string value.
   */
  public String value() {
    return value;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return KEYWORD;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(value);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(value);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Keyword) ? safeEquals(value, ((Keyword)obj).value) : false;
  }

  @Override
  public String toString() {
    return ModelUtils.toString(this);
  }

  @Override
  public int hashCode() {
    return ModelUtils.notHashable();
  }

}
