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

package com.squarespace.less;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.BufferStack;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.FunctionTable;
import com.squarespace.less.exec.MixinResolver;
import com.squarespace.less.exec.NodeRenderer;
import com.squarespace.less.exec.RenderEnv;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.parse.LessImporter;


/**
 * Context for a single LESS parse/compile operation.  Used by implementation classes
 * to obtain access to compiler-wide state:
 *  - compile options
 *  - node renderer
 *  - reusable compiler
 *  - reusable buffer stack
 *  etc.
 */
public class LessContext {

  private static final NodeBuilder DEFAULT_NODE_BUILDER = new DefaultNodeBuilder();

  private final BufferStack bufferStack = new BufferStack(this);

  private final MixinResolver mixinResolver = new MixinResolver();

  private final LessStats stats = new LessStats();

  /**
   * Recovery warnings collected during parse/eval/render in safe mode.
   * Drained by the renderer into the output's WARNING comments.
   */
  private final List<String> warnings = new ArrayList<>();

  private final LessOptions opts;

  private LessCompiler compiler;

  private LessImporter importer;

  private FunctionTable functionTable;

  private NodeBuilder nodeBuilder = DEFAULT_NODE_BUILDER;

  private int importDepth;

  private int mixinDepth;

  // Fresh options per context: a shared static instance would let one
  // caller's mutations leak into every other context in the JVM.
  public LessContext() {
    this(new LessOptions());
  }

  public LessContext(LessOptions opts) {
    this(opts, null);
  }

  public LessContext(LessOptions opts, LessLoader loader) {
    this(opts, loader, null);
  }

  public LessContext(LessOptions opts, LessLoader loader, Map<Path, Stylesheet> preCache) {
    this.opts = opts;
    this.importer = new LessImporter(this, loader, preCache);
  }

  public LessOptions options() {
    return opts;
  }

  public NodeBuilder nodeBuilder() {
    return nodeBuilder;
  }

  public void setNodeBuilder(NodeBuilder builder) {
    this.nodeBuilder = builder;
  }

  public MixinResolver mixinResolver() {
    return mixinResolver;
  }

  public void sanityCheck() {
    bufferStack.sanityCheck();
  }

  public LessCompiler compiler() {
    return compiler;
  }

  public void setCompiler(LessCompiler compiler) {
    this.compiler = compiler;
    this.functionTable = compiler.functionTable();
  }

  public LessImporter importer() {
    return importer;
  }

  public Function findFunction(String symbol) {
    return (functionTable != null) ? functionTable.get(symbol) : null;
  }

  public LessStats stats() {
    return stats;
  }

  /**
   * Records a recovery warning.
   */
  public void addWarning(String warning) {
    warnings.add(warning);
  }

  /**
   * The recovery warnings recorded so far (unmodifiable view).
   */
  public List<String> warnings() {
    return warnings;
  }

  /**
   * Returns and clears the recovery warnings (called by the renderer once
   * per render pass so repeated renders do not duplicate them).
   */
  public List<String> drainWarnings() {
    List<String> drained = new ArrayList<>(warnings);
    warnings.clear();
    return drained;
  }

  public Buffer acquireBuffer() {
    return bufferStack.acquireBuffer();
  }

  public void returnBuffer() {
    bufferStack.returnBuffer();
  }

  /**
   * Reset the reusable-buffer stack. Call at render start so a prior
   * failed render cannot shift which buffers future callers get.
   */
  public void resetBuffers() {
    bufferStack.reset();
  }

  public Buffer newBuffer() {
    return new Buffer(opts.indent(), opts.compress());
  }

  public ExecEnv newEnv() {
    return new ExecEnv(this);
  }

  public RenderEnv newRenderEnv() {
    return new RenderEnv(this);
  }

  public LessErrorInfo newError(LessErrorType type) {
    return new LessErrorInfo(type);
  }

  public String render(Node node) {
    return NodeRenderer.render(this, node);
  }

  public void render(Buffer buf, Node node) throws LessException {
    NodeRenderer.render(buf, node);
  }

  /**
   * Reset depth counters between compiles. A failed compile can leave them
   * nonzero, which would make a later compile on the same context fail.
   */
  public void resetDepthCounters() {
    this.importDepth = 0;
    this.mixinDepth = 0;
  }

  public void enterImport() {
    this.importDepth++;
    stats.importDepth(this.importDepth);
  }

  public void exitImport() {
    this.importDepth--;
  }

  public int importDepth() {
    return this.importDepth;
  }

  public void enterMixin() {
    this.mixinDepth++;
    stats.mixinDepth(this.mixinDepth);
  }

  public void exitMixin() {
    this.mixinDepth--;
  }

  public int mixinDepth() {
    return this.mixinDepth;
  }

}
