package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.FlexList;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.BlockNode;
import com.squarespace.v6.template.less.model.Definition;


/**
 * Represents the current execution state. It also holds a reference to the
 * compile-wide context.
 */
public class ExecEnv {
  
  private Context ctx;

  private FlexList<Block> frames;

  private FlexList<String> rootPath;
  
  public ExecEnv(Context ctx) {
    this(ctx, new FlexList<Block>(64), new FlexList<String>(3));
  }
  
  public ExecEnv(Context ctx, FlexList<Block> initialStack, FlexList<String> initialRootPath) {
    this.ctx = ctx;
    this.frames = initialStack;
    this.rootPath = initialRootPath;
  }
  
  public Context context() {
    return ctx;
  }
  
  public ExecEnv copy() {
    return new ExecEnv(ctx, frames.copy(), rootPath.copy());
  }

  public int depth() {
    return frames.size();
  }
  
  public void append(FlexList<Block> other) {
    frames.append(other);
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
