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

import java.util.List;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.ExtendList;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Stylesheet;


/**
 * Manages an implicit stack of {@link RenderFrame} instances, used during rendering.
 */
public class RenderEnv {

  /**
   * Reference to the current {@link LessContext}
   */
  private final LessContext ctx;

  /**
   * Current media scope extend context, if any.
   */
  private final FlexList<ExtendContext> mediaExtendContext = new FlexList<>(4);

  /**
   * Current {@link RenderFrame} we're pointing to in the implicit stack.
   */
  private RenderFrame frame;

  /**
   * Global extend context, if any.
   */
  private ExtendContext globalExtendContext;

  /**
   * Current stack depth.
   */
  private int depth;

  /**
   * Builds an empty {@code RenderEnv} instance with the given context.
   */
  public RenderEnv(LessContext ctx) {
    this(ctx, null);
  }

  /**
   * Builds a {@code RenderEnv} instance pointing to the given {@link RenderFrame}
   * with the given context.
   */
  public RenderEnv(LessContext ctx, RenderFrame frame) {
    this.ctx = ctx;
    this.frame = frame;
  }

  /**
   * Returns the {@link LessContext}.
   */
  public LessContext context() {
    return ctx;
  }

  /**
   * Returns the current {@link RenderFrame}.
   */
  public RenderFrame frame() {
    return frame;
  }

  public void indexSelector(Selector selector) {
    if (!mediaExtendContext.isEmpty()) {
      mediaExtendContext.last().index(selector);
    } else if (globalExtendContext != null) {
      globalExtendContext.index(selector);
    }
  }

  public void indexSelector(Selectors selectors, ExtendList extendList) {
    if (!mediaExtendContext.isEmpty()) {
      mediaExtendContext.last().index(selectors, extendList);
    } else if (globalExtendContext != null) {
      globalExtendContext.index(selectors, extendList);
    }
  }

  /**
   * Extends the selector list by searching the extend context. We search
   * up the stack to find all extend contexts, since multiple may apply.
   */
  public Selectors extend(Selectors selectors) {
    List<Selector> extended = null;

    if (!mediaExtendContext.isEmpty()) {
      extended = mediaExtendContext.last().extend(selectors, extended);
    }

    if (globalExtendContext != null) {
      extended = globalExtendContext.extend(selectors, extended);
    }

    if (extended == null || extended.isEmpty()) {
      return selectors;
    }

    // We matched some selectors in the extend index. Append them.
    Selectors result = selectors.copy();
    for (Selector selector : extended) {
      result.add(selector);
    }
    return result;
  }

  /**
   * Pushes a {@link BlockNode} onto the render stack.
   */
  public void push(BlockNode blockNode) throws LessException {
    if (blockNode == null) {
      throw new LessInternalException("Serious error: blockNode cannot be null.");
    }

    // Ensure that selectors / features are evaluated outside the block they're attached to.
    // Otherwise they may bind to variables from nested scopes.
    Selectors selectors = null;
    Features features = null;

    NodeType blockType = blockNode.type();
    switch (blockType) {
      case MEDIA:
        features = ((Media)blockNode).features();
        mediaExtendContext.push(((Media)blockNode).extendContext());
        break;

      case RULESET:
        selectors = ((Ruleset)blockNode).selectors();
        break;

      case STYLESHEET:
        globalExtendContext = ((Stylesheet)blockNode).extendContext();
        break;

      default:
        break;
    }

    depth++;
    frame = new RenderFrame(frame, blockNode, depth);

    if (blockType.equals(NodeType.BLOCK_DIRECTIVE)) {
      frame.pushEmptySelectors();
    } else if (selectors != null) {
      frame.mergeSelectors(selectors);
    } else if (features != null) {
      frame.mergeFeatures(features);
    }
  }

  /**
   * Removes the current {@link BlockNode} from the render stack, replacing
   * it with its parent.
   */
  public void pop() {
    if (frame == null) {
      throw new LessInternalException("Serious error: popped past the last frame!");
    }

    if (frame.blockNode() instanceof Media) {
      mediaExtendContext.pop();
    }

    depth--;
    frame = frame.parent();
  }

}
