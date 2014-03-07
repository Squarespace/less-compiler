package com.squarespace.less.exec;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Definition;


/**
 * Represents the current execution environment.
 */
public class ExecEnv {

  private final Context ctx;

  private final FlexList<Block> frames;

  private FlexList<String> warnings;

  public ExecEnv(Context ctx) {
    this(ctx, new FlexList<Block>(64), null);
  }

  public ExecEnv(Context ctx, FlexList<Block> initialStack) {
    this(ctx, initialStack, null);
  }

  public ExecEnv(Context ctx, FlexList<Block> initialStack, FlexList<String> warnings) {
    this.ctx = ctx;
    this.frames = initialStack;
    this.warnings = warnings;
  }

  public Context context() {
    return ctx;
  }

  public ExecEnv copy() {
    return new ExecEnv(ctx, frames.copy(), warnings);
  }

  public int depth() {
    return frames.size();
  }

  public void append(FlexList<Block> other) {
    frames.append(other);
  }

  public void addWarning(String warning) {
    if (warnings == null) {
      warnings = new FlexList<>();
    }
    warnings.append(warning);
  }

  public String warnings() {
    if (warnings == null || warnings.isEmpty()) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    int size = warnings.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      buf.append(warnings.get(i));
    }
    warnings.clear();
    return buf.toString();
  }

  public FlexList<Block> frames() {
    return frames;
  }

  /**
   * Iterate up the stack, trying to find the given variable definition in each block.
   */
  public Definition resolveDefinition(String name) throws LessException {
    int size = frames.size();
    for (int i = size - 1; i >= 0; i--) {
      Definition def = frames.get(i).resolveDefinition(name);
      if (def != null) {
        return def;
      }
    }
    return null;
  }

  /**
   * Iterate up the stack, trying to resolve the mixin against each block.
   */
  public boolean resolveMixins(MixinResolver resolver) throws LessException {
    int size = frames.size();
    for (int i = size - 1; i >= 0; i--) {
      if (resolver.match(frames.get(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Push a block onto the stack.
   */
  public void push(BlockNode blockNode) throws LessException {
    frames.push(blockNode.block());
  }

  /**
   * Pop the current block off the stack.
   */
  public void pop() {
    frames.pop();
  }

}
