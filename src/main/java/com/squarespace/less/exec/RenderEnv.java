package com.squarespace.less.exec;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selectors;


/**
 * Manages an implicit stack of RenderFrame instances, used during LESS rendering.
 */
public class RenderEnv {

  private final Context ctx;

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
