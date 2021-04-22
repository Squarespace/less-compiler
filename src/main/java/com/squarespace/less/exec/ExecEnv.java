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
import java.util.Map;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockNode;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Node;


/**
 * Represents the current execution environment.
 */
public class ExecEnv {

  /**
   * Context for the current compile.
   */
  private final LessContext ctx;

  /**
   * Stack frames for execution.
   */
  private final FlexList<Block> frames;

  /**
   * List of warnings emitted during execution.
   */
  private FlexList<String> warnings;

  /**
   * Exception that terminated execution, if any.
   */
  private LessException error;

  /**
   * Constructs an instance associated with the given compile context.
   */
  public ExecEnv(LessContext ctx) {
    this(ctx, new FlexList<Block>(64), null);
  }

  /**
   * Constructs an instance associated with the given compile context and
   * initial stack contents.
   */
  public ExecEnv(LessContext ctx, FlexList<Block> initialStack) {
    this(ctx, initialStack, null);
  }

  /**
   * Constructs an instance associated with the given compile context,
   * initial stack contents, and warning list.
   */
  public ExecEnv(LessContext ctx, FlexList<Block> initialStack, FlexList<String> warnings) {
    this.ctx = ctx;
    this.frames = initialStack;
    this.warnings = warnings;
  }

  /**
   * Returns the context associated with this compile.
   */
  public LessContext context() {
    return ctx;
  }

  /**
   * Returns a new {@link ExecEnv} instance with a copy of the stack frames and warnings.
   */
  public ExecEnv copy() {
    return new ExecEnv(ctx, frames.copy(), warnings);
  }

  /**
   * Current stack depth.
   */
  public int depth() {
    return frames.size();
  }

  /**
   * Indicates an error has been produced.
   */
  public boolean hasError() {
    return error != null;
  }

  /**
   * Returns the execution error, if any.
   */
  public LessException error() {
    return error;
  }

  /**
   * Sets the execution error.
   */
  public void error(LessException exc) {
    error = exc;
  }

  /**
   * Pushes a list of frames onto the stack.
   */
  public void append(FlexList<Block> other) {
    frames.append(other);
  }

  /**
   * Adds a warning to the list.
   */
  public void addWarning(String warning) {
    if (warnings == null) {
      warnings = new FlexList<>();
    }
    warnings.append(warning);
  }

  /**
   * Return the formatted list of warnings.
   */
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

  /**
   * Returns the stack frames.
  */
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

      // Future: pragma to skip over circular references, looking in a higher stack frame.
      // To do that we check if the definition is evaluating (circular) and skip it:
      //
      // if (def != null && pragmaSkipCircular() && !def.evaluating()) {
      // ...

      // If definition exists, return it
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
    List<String> path = resolver.callPath;
    String prefix = path.get(0);
    int end = frames.size() - 1;
    for (int i = end; i >= 0; i--) {
      // Prune the mixin search space at the top level. If no paths
      // have our desired prefix we skip the block entirely.
      Block block = frames.get(i);
      Map<String, List<Node>> mixins = block.mixins();
      if (mixins != null && mixins.containsKey(prefix)) {
        if (resolver.match(block)) {
          return true;
        }
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

  /**
   * Dump all variable definitions in each block of the stack. Used for
   * debugging in a pinch.
   */
//  public String dumpDefinitions() {
//    Buffer buf = new Buffer(4);
//    int size = frames.size();
//    for (int i = size - 1; i >= 0; i--) {
//      frames.get(i).dumpDefs(buf);
//      buf.incrIndent();
//    }
//    return buf.toString();
//  }
}
