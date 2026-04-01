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

import com.squarespace.less.LessException;
import com.squarespace.less.core.Constants;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Feature;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;


/**
 * A stack frame used for rendering.
 *
 * It tracks the current combined {@link Selector} and media {@link Feature} sets
 * as we drill deeper into nested {@link Ruleset} and {@link Media} nodes.
 */
public class RenderFrame {

  private final RenderFrame parent;
  private final BlockNode blockNode;
  private final int depth;
  private Selectors selectors;
  private Features features;

  public RenderFrame() {
    this(null, null, 0);
  }

  public RenderFrame(RenderFrame parent, BlockNode blockNode, int depth) {
    this.parent = parent;
    this.blockNode = blockNode;
    this.depth = depth;
  }

  public BlockNode blockNode() {
    return blockNode;
  }

  public RenderFrame parent() {
    return parent;
  }

  public int depth() {
    return depth;
  }

  /**
   * Returns the current {@link Selectors} for this stack frame.
   */
  public Selectors selectors() {
    if (selectors != null) {
      return selectors;
    }
    return parent == null ? Constants.EMPTY_SELECTORS : parent.selectors();
  }

  /**
   * Returns the current media {@link Features} for this stack frame.
   */
  public Features features() {
    if (features != null) {
      return features;
    }
    return parent == null ? Constants.EMPTY_FEATURES : parent.features();
  }

  /**
   * Combines this set of {@link Selectors} with its parent's. When the
   * combined selector exceeds the complexity limit and
   * {@code fallbackOnOverflow} is set, the failure is swallowed and the
   * current selector is dropped. Otherwise it fails the compile.
   */
  public void mergeSelectors(Selectors current, boolean fallbackOnOverflow) throws LessException {
    mergeSelectors(current, fallbackOnOverflow, false, null);
  }

  /**
   * Merge with optional truncation (best-effort recovery): when
   * {@code truncateOnOverflow} and the fixed level are active, a
   * complexity overflow truncates the combined selectors at the limit
   * instead of falling back or failing. The {@code truncated} flag
   * lets the caller warn.
   *
   * The shared combined-set complexity budget is fixed-level only. At the
   * legacy levels ({@code fallbackOnOverflow}) the released compiler reset
   * the complexity counter per current-selector flatten call, so the
   * overflow decision stays per-call. Otherwise a nested combination
   * whose per-selector sums each fit under the limit (e.g. 32 x 21 x 3 =
   * 6048 combined, 2016 per call) overflows here but not in the release,
   * and the fallback below would drop the whole current selector list.
   */
  public void mergeSelectors(Selectors current, boolean fallbackOnOverflow, boolean truncateOnOverflow,
      boolean[] truncated) throws LessException {
    Selectors ancestors = (parent == null) ? Constants.EMPTY_SELECTORS : parent.selectors();
    if (current == null || current.isEmpty()) {
      this.selectors = ancestors;
    } else {
      try {
        this.selectors = SelectorUtils.combine(ancestors, current, truncateOnOverflow,
            !fallbackOnOverflow, truncated);
      } catch (LessException e) {
        if (fallbackOnOverflow) {
          // Released (legacy) overflow contract: the failure is swallowed.
          // Nested combinators degrade to the parent selector. A FLAT
          // top-level list has no ancestors, so keeping the current list
          // preserves the released output (the released counter was
          // per-flatten-call, so flat lists could never overflow, and the
          // shared budget must not silently drop them).
          this.selectors = ancestors.isEmpty() ? current : ancestors;
        } else {
          throw e;
        }
      }
    }
  }

  /**
   * Block directives act as a root scope, so prevent selector inheritance.
   */
  public void pushEmptySelectors() {
    this.selectors = Constants.EMPTY_SELECTORS;
  }

  /**
   * Combines this set of media features with its parent's.
   */
  public void mergeFeatures(Features current) {
    Features ancestors = (parent == null) ? Constants.EMPTY_FEATURES : parent.features();
    if (current == null || current.isEmpty()) {
      this.features = ancestors;
    } else {
      this.features = FeatureUtils.combine(ancestors, current);
    }
  }

  @Override
  public String toString() {
    return "RenderFrame depth=" + depth;
  }

  /**
   * Debug method - returns a string containing info on this frame and its parents,
   * indented based on depth.
   */
//  public String repr() {
//    Buffer buf = new Buffer(4);
//    repr(buf);
//    return buf.toString();
//  }

//  private void repr(Buffer buf) {
//    buf.indent().append("Frame depth=").append(depth).append('\n');
//    if (blockNode != null) {
//      blockNode.block().dumpDefs(buf);
//    }
//    if (parent != null) {
//      buf.incrIndent();
//      parent.repr(buf);
//      buf.decrIndent();
//    }
//  }

}
