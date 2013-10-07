package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.model.BlockNode;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selectors;


/**
 * Manages an implicit stack of RenderFrame instances, used during LESS rendering.
 */
public class RenderEnv {

  private Context ctx;
  
  private RenderFrame frame;

  private int depth;
  
  public RenderEnv(Context ctx) {
    this(ctx, null);
  }

  public RenderEnv(Context ctx, RenderFrame frame) {
    this.ctx = ctx;
    this.frame = frame;
  }
  
  public Context context() {
    return ctx;
  }
  
  public RenderFrame frame() {
    return frame;
  }
  
  public String render(Node node) throws LessException {
    return ctx.render(node);
  }
  
  public void render(Buffer buf, Node node) throws LessException {
    ctx.render(buf, node);
  }
  
  public void push(BlockNode blockNode) throws LessException {
    // Ensure that selectors / features are evaluated outside the block they're attached to.
    Selectors selectors = null;
    Features features = null;
    if (blockNode != null) {
      if (blockNode.is(NodeType.RULESET)) {
        selectors = (Selectors) ((Ruleset)blockNode).selectors();
      } else if (blockNode.is(NodeType.MEDIA)) {
        features = (Features) ((Media)blockNode).features();
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
      throw new RuntimeException("Serious error: popped past the last frame!");
    }
    depth--;
    frame = frame.parent();
  }
  
}
