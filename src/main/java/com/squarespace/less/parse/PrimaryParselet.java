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

package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.PRIMARY_SUB;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;


public class PrimaryParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Block block = new Block();
    Node node = null;
    stm.skipEmpty();

    // Save current stream position before parsing each primary rule type.
    Mark position = stm.mark();
    while ((node = stm.parse(PRIMARY_SUB)) != null) {
      // Assign stream position to successfully-parsed rule.
      node.setLineOffset(position.lineOffset);
      node.setCharOffset(position.charOffset);

      // Importing a stylesheet can return a block, so we need to expand it here.
      if (node.is(NodeType.BLOCK)) {
        block.appendBlock((Block)node);
      } else {
        block.appendNode(node);
      }
      stm.skipEmpty();
      stm.mark(position);
    }
    stm.skipEmpty();
    return block;
  }

}
