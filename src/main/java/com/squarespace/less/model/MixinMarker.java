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

import java.nio.file.Path;


/**
 * A dummy node which is placed into a block to indicate where a {@link MixinCall}
 * generated nodes begin and end.
 */
public class MixinMarker implements Node {

  /**
   * Mixin call whose beginning and end are being marked.
   */
  protected final MixinCall call;

  /**
   * The mixin definition being called.
   */
  protected final BlockNode definition;

  /**
   * Indicates whether this marker is the beginning or end.
   */
  protected final boolean beginning;

  /**
   * Path to the file in which the mixin call is defined.
   */
  protected Path fileName;

  /**
   * Constructs a mixin marker with the given call, definition, and beginning/end
   * indicator.
   */
  public MixinMarker(MixinCall call, BlockNode definition, boolean begin) {
    this.call = call;
    this.definition = definition;
    this.beginning = begin;
  }

  /**
   * Returns the mixin call associated with this marker.
   */
  public MixinCall mixinCall() {
    return call;
  }

  /**
   * Returns the mixin definition associated with this marker.
   */
  public BlockNode mixinDefinition() {
    return definition;
  }

  /**
   * Returns the path to the file in which this mixin call was defined.
   */
  public Path fileName() {
    return fileName;
  }

  /**
   * Sets the path to the filename in which this mixin call was defined.
   */
  public void fileName(Path path) {
    this.fileName = path;
  }

  /**
   * Indicates if this marker is for the beginning or end of the call.
   */
  public boolean beginning() {
    return beginning;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.MIXIN_MARKER;
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
