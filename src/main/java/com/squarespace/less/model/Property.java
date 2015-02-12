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
 * The property name for a {@link Rule} or {@link Feature}.
 */
public class Property extends BaseNode {

  /**
   * Name of the property.
   */
  protected final String name;

  /**
   * Quick flag to detect special "font" property.
   */
  protected final boolean isFont;

  /**
   * Mode for merging properties having the same name.
   */
  protected PropertyMergeMode mergeMode;


  /**
   * Constructs a property with the given name, without a merge mode.
   */
  public Property(String name) {
    this(name, PropertyMergeMode.NONE);
  }

  /**
   * Constructs a property with the given name and merge mode.
   */
  public Property(String name, PropertyMergeMode mergeMode) {
    this.name = name;
    this.mergeMode = mergeMode;
    this.isFont = name.equals("font");
  }

  /**
   * Returns the property's name.
   */
  public String name() {
    return name;
  }

  /**
   * Indicates whether this property is named "font". Special math mode
   * is enabled for font rules.
   */
  public boolean isFont() {
    return isFont;
  }

  /**
   * Mode for merging properties having the same name.
   */
  public PropertyMergeMode mergeMode() {
    return mergeMode;
  }

  /**
   * Sets the merge mode for this property.
   */
  public void mergeMode(PropertyMergeMode merge) {
    this.mergeMode = merge;
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
    if (mergeMode == PropertyMergeMode.COMMA) {
      buf.append('+');
    } else if (mergeMode == PropertyMergeMode.SPACE) {
      buf.append("+_");
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
    if (mergeMode != PropertyMergeMode.NONE) {
      buf.append(" MERGE_").append(mergeMode.name());
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Property) {
      Property other = (Property)obj;
      return mergeMode == other.mergeMode && safeEquals(name, other.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
