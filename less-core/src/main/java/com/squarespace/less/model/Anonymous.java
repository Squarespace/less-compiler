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

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;


/**
 * Anonymous nodes are used any time an opaque string.
 */
public class Anonymous extends BaseNode {

  /**
   * Default value for anonymous nodes are the empty string.
   */
  private static final String EMPTY = "";

  /**
   * String value to be emitted.
   */
  protected final String value;

  /**
   * Constructs an anonymous string with an empty string.
   */
  public Anonymous() {
    this.value = EMPTY;
  }

  /**
   * Constructs an anonymous string with the given {@code value}.
   */
  public Anonymous(String value) {
    if (value == null) {
      throw new LessInternalException("Serious error: value cannot be null.");
    }
    this.value = value;
  }

  /**
   * Returns the string value.
   */
  public String value() {
    return value;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.ANONYMOUS;
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
    buf.append(" \"");
    if (value != null) {
      buf.append(StringEscapeUtils.escapeJava(value));
    }
    buf.append('"');
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Anonymous) ? safeEquals(value, ((Anonymous)obj).value) : false;
  }

  @Override
  public int hashCode() {
    return hashCode == 0 ? buildHashCode(value) : hashCode;
  }

}
