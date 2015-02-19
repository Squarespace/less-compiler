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

import com.squarespace.less.core.Buffer;


public class Dummy extends BaseNode {

  /**
   * Returns a Dummy instance that has the position of the given node.
   */
  public static Dummy fromNode(BaseNode node) {
    Dummy dummy = new Dummy();
    dummy.copyBase(node);
    return dummy;
  }

  /**
   * @see BaseNode#type()
   */
  @Override
  public NodeType type() {
    return NodeType.DUMMY;
  }

  /**
   * @see BaseNode#repr(Buffer)
   */
  @Override
  public void repr(Buffer buf) {
    // No-op
  }

  /**
   * @see BaseNode#modelRepr(Buffer)
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
  }

}
