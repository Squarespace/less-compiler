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
   * Current media scope extend index, if any.
   */
  private final FlexList<ExtendIndex> mediaExtendStack = new FlexList<>(4);

  /**
   * Current {@link RenderFrame} we're pointing to in the implicit stack.
   */
  private RenderFrame frame;

  /**
   * Global extend index, if any.
   */
  private ExtendIndex globalExtendIndex;

  /**
   * Matches selectors against the extend indexes.
   */
  private ExtendMatcher extendMatcher;

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
    if (!mediaExtendStack.isEmpty()) {
      mediaExtendStack.last().index(selector);
    } else if (globalExtendIndex != null) {
      globalExtendIndex.index(selector);
    }
  }

  public void indexSelector(Selectors selectors, ExtendList extendList) {
    if (!mediaExtendStack.isEmpty()) {
      mediaExtendStack.last().index(selectors, extendList);
    } else if (globalExtendIndex != null) {
      globalExtendIndex.index(selectors, extendList);
    }
  }

  /**
   * Perform extend expression matching against the given selector. It will
   * match the current Media-scope index (if any) followed by the global index.
   * It returns a list containing the original and generated selectors.
   */
  public List<Selector> extend(Selectors selectors) {
    List<Selector> extended = null;

    if (extendMatcher == null) {
      extendMatcher = new ExtendMatcher();
    }

    if (!mediaExtendStack.isEmpty()) {
      extended = extendMatcher.extend(mediaExtendStack.last(), selectors, extended);
    }

    if (globalExtendIndex != null) {
      extended = extendMatcher.extend(globalExtendIndex, selectors, extended);
    }

    return extended;
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
        ExtendIndex mediaExtendIndex = ((Media)blockNode).extendIndex();
        mediaExtendStack.push(mediaExtendIndex);

        // Special case for a MEDIA block. We may have globally-defined extend
        // expressions that extend other extend expressions, so we need to resolve
        // these before we render the rules under this MEDIA scope.
        mediaExtendIndex.resolveSelfExtends(globalExtendIndex.capturedExtends());
        break;

      case RULESET:
        selectors = ((Ruleset)blockNode).selectors();
        break;

      case STYLESHEET:
        globalExtendIndex = ((Stylesheet)blockNode).extendIndex();
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

    BlockNode blockNode = frame.blockNode();
    if (blockNode instanceof Media) {
      ExtendIndex index = mediaExtendStack.pop();
      index.resolveSelfExtends();
    } else if (blockNode instanceof Stylesheet) {
      globalExtendIndex.resolveSelfExtends();
    }

    depth--;
    frame = frame.parent();
  }

}
