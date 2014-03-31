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

package com.squarespace.less.exec;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selectors;


/**
 * Manages an implicit stack of RenderFrame instances, used during LESS rendering.
 */
public class RenderEnv {

  private final LessContext ctx;

  private RenderFrame frame;

  private int depth;

  public RenderEnv(LessContext ctx) {
    this(ctx, null);
  }

  public RenderEnv(LessContext ctx, RenderFrame frame) {
    this.ctx = ctx;
    this.frame = frame;
  }

  public LessContext context() {
    return ctx;
  }

  public RenderFrame frame() {
    return frame;
  }

  public void push(BlockNode blockNode) throws LessException {
    if (blockNode == null) {
      throw new LessInternalException("Serious error: blockNode cannot be null.");
    }

    // Ensure that selectors / features are evaluated outside the block they're attached to.
    Selectors selectors = null;
    Features features = null;
    if (blockNode != null) {
      if (blockNode.is(NodeType.RULESET)) {
        selectors = ((Ruleset)blockNode).selectors();
      } else if (blockNode.is(NodeType.MEDIA)) {
        features = ((Media)blockNode).features();
      }
    }

    depth++;
    frame = new RenderFrame(frame, blockNode, depth);

    if (blockNode.is(NodeType.BLOCK_DIRECTIVE)) {
      frame.pushEmptySelectors();
    } else if (selectors != null) {
      frame.mergeSelectors(selectors);
    } else if (features != null) {
      frame.mergeFeatures(features);
    }
  }

  public void pop() {
    if (frame == null) {
      throw new LessInternalException("Serious error: popped past the last frame!");
    }
    depth--;
    frame = frame.parent();
  }

}