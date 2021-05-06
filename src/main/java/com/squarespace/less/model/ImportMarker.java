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


/**
 * A dummy node which is placed into a block to indicate where an IMPORT
 * statements imported rules begins and ends.
 */
public class ImportMarker implements Node {

  /**
   * Import statement we're marking.
   */
  protected final Import importStatement;

  /**
   * Whether this marks the beginning or end.
   */
  protected final boolean beginning;

  /**
   * Constructs a marker for the given import statement, setting the
   * begin/end flag.
   */
  public ImportMarker(Import imp, boolean begin) {
    this.importStatement = imp;
    this.beginning = begin;
  }

  /**
   * Returns the import statement being marked.
   */
  public Import importStatement() {
    return importStatement;
  }

  /**
   * Indicates whether this marks the beginning or end.
   */
  public boolean beginning() {
    return beginning;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.IMPORT_MARKER;
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
