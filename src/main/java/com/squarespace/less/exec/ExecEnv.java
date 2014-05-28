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
import com.squarespace.less.core.FlexList;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Definition;


/**
 * Represents the current execution environment.
 */
public class ExecEnv {

  private final LessContext ctx;

  private final FlexList<Block> frames;

  private FlexList<String> warnings;

  private LessException error;

  public ExecEnv(LessContext ctx) {
    this(ctx, new FlexList<Block>(64), null);
  }

  public ExecEnv(LessContext ctx, FlexList<Block> initialStack) {
    this(ctx, initialStack, null);
  }

  public ExecEnv(LessContext ctx, FlexList<Block> initialStack, FlexList<String> warnings) {
    this.ctx = ctx;
    this.frames = initialStack;
    this.warnings = warnings;
  }

  public LessContext context() {
    return ctx;
  }

  public ExecEnv copy() {
    return new ExecEnv(ctx, frames.copy(), warnings);
  }

  public int depth() {
    return frames.size();
  }

  public boolean hasError() {
    return error != null;
  }

  public LessException error() {
    return error;
  }

  public void error(LessException exc) {
    error = exc;
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
