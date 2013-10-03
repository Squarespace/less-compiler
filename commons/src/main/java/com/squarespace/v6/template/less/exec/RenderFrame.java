package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.Constants;
import com.squarespace.v6.template.less.model.BlockNode;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Selectors;


/**
 * Rendering stack frame.  Tracks the current combined selector and media feature sets
 * as we drill deeper into RULESET and MEDIA nodes.
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
   * Returns the current selectors for this stack frame.
   */
  public Selectors selectors() {
    if (selectors != null) {
      return selectors;
    }
    return parent == null ? Constants.EMPTY_SELECTORS : parent.selectors();
  }

  /**
   * Returns the current media features for this stack frame.
   */
  public Features features() {
    if (features != null) {
      return features;
    }
    return parent == null ? Constants.EMPTY_FEATURES : parent.features();
  }

  /**
   * Combines this set of ruleset/mixin selectors with its parent's.
   */
  public void mergeSelectors(Selectors current) {
    Selectors ancestors = (parent == null) ? Constants.EMPTY_SELECTORS : parent.selectors();
    if (current == null || current.isEmpty()) {
      this.selectors = ancestors;
    } else {
      this.selectors = SelectorUtils.combine(ancestors, current);
    }
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
  public String repr() {
    Buffer buf = new Buffer(4);
    repr(buf);
    return buf.toString();
  }
  
  private void repr(Buffer buf) {
    buf.indent().append("Frame depth=").append(depth).append('\n');
    if (blockNode != null) {
      blockNode.block().dumpDefs(buf);
    }
    if (parent != null) {
      buf.incrIndent();
      parent.repr(buf);
      buf.decrIndent();
    }
  }
  
}
