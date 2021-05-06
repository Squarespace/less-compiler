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
import static com.squarespace.less.model.NodeType.UNICODE_RANGE;

import com.squarespace.less.core.Buffer;


/**
 * A wrapper around a CSS Unicode character range.
 */
public class UnicodeRange implements Node {

  /**
   * String representing the Unicode character range.
   */
  protected final String value;

  /**
   * Constructs a Unicode range node wrapping the given string.
   * @param value
   */
  public UnicodeRange(String value) {
    this.value = value;
  }

  /**
   * Returns the Unicode range string.
   */
  public String value() {
    return value;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return UNICODE_RANGE;
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
    buf.append('(').append(value).append(')');
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof UnicodeRange) ? safeEquals(value, ((UnicodeRange)obj).value) : false;
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
