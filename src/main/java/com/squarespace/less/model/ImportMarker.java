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
public class ImportMarker extends BaseNode {

  protected final Import importStatement;

  protected final boolean beginning;

  public ImportMarker(Import imp, boolean begin) {
    this.importStatement = imp;
    this.beginning = begin;
  }

  public Import importStatement() {
    return importStatement;
  }

  public boolean beginning() {
    return beginning;
  }

  @Override
  public NodeType type() {
    return NodeType.IMPORT_MARKER;
  }

}
