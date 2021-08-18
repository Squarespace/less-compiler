/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

package com.squarespace.less.exec;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Stylesheet;


/**
 * Scans a stylesheet recursively, performing indexing.
 *
 * TODO: this may be optimized by indexing during evaluation.
 */
public class LessIndexer {

  private final RenderEnv env;

  public LessIndexer(RenderEnv env) {
    this.env = env;
  }

  public void index(Stylesheet stylesheet) throws LessException {
    indexExtends((BlockNode)stylesheet);
  }

  /**
   * Scan the stylesheet for extends and index them.
   */
  private void indexExtends(BlockNode blockNode) throws LessException {
    env.push(blockNode);

    // If one of the selectors has an extend list, index it.
    if (blockNode instanceof Ruleset) {
      Selectors selectors = env.frame().selectors();
      if (selectors.hasExtend()) {
        List<Selector> _selectors = selectors.selectors();
        int size = _selectors.size();
        for (int i = 0; i < size; i++) {
          Selector selector = _selectors.get(i);
          if (selector.hasExtend()) {
            env.indexSelector(selector);
          }
        }
      }
    }

    // Iterate looking for rule-level extends and other block nodes.
    // We use the flags to try to avoid scanning blocks unnecessarily.
    if (canIndex(blockNode)) {
      Block block = blockNode.block();
      FlexList<Node> rules = block.rules();
      int size = rules.size();
      for (int i = 0; i < size; i++) {
        Node node = rules.get(i);

        if (node instanceof BlockNode) {
          // Recurse into the block.
          indexExtends((BlockNode)node);

        } else if (node instanceof ExtendList) {
          // Index the rule-level extend.
          env.indexSelector(env.frame().selectors(), (ExtendList)node);
        }
      }
    }

    env.pop();
  }

  /**
   * The switch below defines a type white list and criteria for entry of a
   * block node for purposes of indexing EXTEND lists.
   *
   * When indexing extends we must ignore block nodes within the tree which
   * are not intended to be rendered. A MIXIN definition exists to be
   * resolved and invoked by a MIXIN_CALL, but the definition itself is
   * never rendered into CSS.
   */
  private static boolean canIndex(BlockNode blockNode) {
    switch (blockNode.type()) {
      case BLOCK_DIRECTIVE:
      case DETACHED_RULESET:
      case MEDIA:
      case RULESET:
      case STYLESHEET:
        Block block = blockNode.block();
        return block.hasNestedBlock() || block.hasNestedExtend();

      default:
        break;
    }
    return false;
  }

}