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
 * A dummy node which is placed into a block to indicate where a MIXIN
 * call's generated nodes begin and end.
 */
public class MixinMarker extends BaseNode {

  private final MixinCall call;

  private final boolean beginning;

  private Path fileName;

  public MixinMarker(MixinCall call, boolean begin) {
    this.call = call;
    this.beginning = begin;
  }

  public MixinCall mixinCall() {
    return call;
  }

  public Path fileName() {
    return fileName;
  }

  public boolean beginning() {
    return beginning;
  }

  public void fileName(Path path) {
    this.fileName = path;
  }

  @Override
  public NodeType type() {
    return NodeType.MIXIN_MARKER;
  }

}
