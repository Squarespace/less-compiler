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
 * The property name for a {@link Rule}
 */
public class Property extends BaseNode {

  /**
   * Name of the property.
   */
  protected final String name;

  /**
   * Constructs a property with the given name,
   */
  public Property(String name) {
    this.name = name;
  }

  /**
   * Returns the property's name.
   */
  public String name() {
    return name;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.PROPERTY;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(name);
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
    return (obj instanceof Property) ? safeEquals(name, ((Property)obj).name) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
