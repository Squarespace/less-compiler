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
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  /**
   * Exact-message dedupe key for the warning ledger: a construct that is
   * evaluated more than once (e.g. a mixin-defining ruleset rendered both
   * as output and as a mixin expansion) can otherwise emit one identical
   * warning per evaluation.
   */
  private final Set<String> warningKeys = new HashSet<>();

  /**
   * Per-compile warning budgets (see allowWarning): how many warnings of
   * each type and in total have been emitted, and how many were
   * suppressed. Reset together with the ledger at compile start so a
   * reused context always gets a fresh budget.
   */
  private final Map<String, Integer> warningEmitted = new HashMap<>();
  private final Map<String, Integer> warningSuppressed = new HashMap<>();
  private int totalWarningEmitted = 0;
  private int totalWarningSuppressed = 0;

  private final LessOptions opts;

  /**
   * Recovery-mode override from the legacy safeMode boolean entry points.
   * Context-scoped: never mutates the caller's options, so a shared
   * LessOptions cannot be poisoned by one boolean caller. The override
   * persists for the lifetime of this context. The assumed usage is one
   * compile per context (all current callers comply), so a boolean flag
   * set on one compile continues to apply to later compiles that reuse
   * the same context.
   */
  private Boolean safeModeOverride;

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

  /**
   * Recovery mode for this compile: the context override when set
   * (legacy boolean entry points), else the options value. Reading this
   * instead of {@code options().safeMode()} keeps the override
   * context-scoped and never writes through to a shared options object.
   */
  public boolean safeMode() {
    return safeModeOverride != null ? safeModeOverride : opts.safeMode();
  }

  /**
   * Sets the recovery-mode override for this context. Null clears it.
   * Persists until cleared or the context is discarded. One compile per
   * context is the assumed usage pattern.
   */
  public void safeModeOverride(Boolean flag) {
    this.safeModeOverride = flag;
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
   * Coarse per-type budget key derived from the warning text. The
   * drop/skip/truncate ledger surfaces use their own stable prefixes
   * (checked first, they may embed an error-type payload in the
   * message). Evaluation warnings embed their error type
   * ({@code ExecuteError INCOMPATIBLE_UNITS: ...}). Everything else
   * falls into the parse-recovery bucket (including one-off eval
   * strings like the experimental replace() note, acceptable in a
   * catch-all).
   */
  static String warningType(String warning) {
    if (warning.startsWith("eval: dropped")) {
      return "eval-drop";
    }
    if (warning.startsWith("render: skipped") || warning.startsWith("render: truncated")) {
      return "render-skip";
    }
    Matcher m = WARNING_TYPE_PREFIX.matcher(warning);
    if (m.find()) {
      return m.group(1);
    }
    return "parse-recovery";
  }

  private static final Pattern WARNING_TYPE_PREFIX =
      Pattern.compile("(?:SyntaxError|ExecuteError)\\s+([A-Z][A-Z0-9_]+)\\s*:");

  /**
   * Budget gate applied at both warning entry points (the context ledger
   * and the evaluation env): returns true when this warning may be
   * recorded/emitted. Counts suppressed warnings per type and in total
   * when the configured limits are exhausted (0 = unlimited, see
   * LessOptions.maxWarnings / maxWarningsPerType).
   */
  public boolean allowWarning(String warning) {
    int perType = opts.maxWarningsPerType();
    int total = opts.maxWarnings();
    if (perType <= 0 && total <= 0) {
      return true;
    }
    String type = warningType(warning);
    boolean overType = perType > 0 && warningEmitted.getOrDefault(type, 0) >= perType;
    boolean overTotal = total > 0 && totalWarningEmitted >= total;
    if (overType) {
      warningSuppressed.merge(type, 1, Integer::sum);
      return false;
    }
    if (overTotal) {
      totalWarningSuppressed++;
      return false;
    }
    warningEmitted.merge(type, 1, Integer::sum);
    totalWarningEmitted++;
    return true;
  }

  /**
   * One-line summary of warning-budget suppression, or null when nothing
   * was suppressed. Emitted as a single trailing comment at render.
   */
  public String suppressedWarningSummary() {
    if (warningSuppressed.isEmpty() && totalWarningSuppressed == 0) {
      return null;
    }
    StringBuilder buf = new StringBuilder();
    int total = 0;
    for (Map.Entry<String, Integer> entry : warningSuppressed.entrySet()) {
      if (buf.length() > 0) {
        buf.append(", ");
      }
      buf.append(entry.getValue()).append(' ').append(entry.getKey());
      total += entry.getValue();
    }
    if (totalWarningSuppressed > 0) {
      if (buf.length() > 0) {
        buf.append(", ");
      }
      buf.append(totalWarningSuppressed).append(" overall");
      total += totalWarningSuppressed;
    }
    int perType = opts.maxWarningsPerType();
    int overall = opts.maxWarnings();
    StringBuilder limits = new StringBuilder();
    if (perType > 0) {
      limits.append("limit ").append(perType).append(" per type");
    }
    if (overall > 0) {
      if (limits.length() > 0) {
        limits.append(", ");
      }
      limits.append("limit ").append(overall).append(" overall");
    }
    return total + " warnings suppressed (" + buf + "); " + limits;
  }

  /**
   * Undoes the budget accounting for one warning that was appended but
   * never surfaced (safe-mode recovery discards a dropped member's
   * pending warnings). Keeps {@code rendered + suppressed == generated}
   * so the trailing summary arithmetic matches what the user sees. A
   * no-op when the budgets are disabled.
   */
  public void rollbackWarning(String warning) {
    int perType = opts.maxWarningsPerType();
    int total = opts.maxWarnings();
    if (perType <= 0 && total <= 0) {
      return;
    }
    if (perType > 0) {
      warningEmitted.compute(warningType(warning), (k, v) -> v == null || v <= 1 ? null : v - 1);
    }
    if (total > 0 && totalWarningEmitted > 0) {
      totalWarningEmitted--;
    }
  }

  /**
   * Records a recovery warning. Exact-message repeats are free (dedupe
   * runs before the budget). The suppressed counts therefore track
   * *distinct* suppressed messages only. Repeats of an already-
   * suppressed message are dropped silently.
   */
  public void addWarning(String warning) {
    // Dedupe first (exact-message repeats are free), then apply the
    // per-compile budgets to genuinely new ledger entries.
    if (warningKeys.add(warning) && allowWarning(warning)) {
      warnings.add(warning);
    }
  }

  /**
   * The recovery warnings recorded so far (the live backing list. They
   * are drained by the renderer, which clears the ledger).
   */
  public List<String> warnings() {
    return warnings;
  }

  /**
   * Returns and clears the recovery warnings. The renderer drains at the
   * start of render() (parse/eval-phase entries surface leading) and
   * once more at its end (render-phase entries trail the output).
   */
  public List<String> drainWarnings() {
    List<String> drained = new ArrayList<>(warnings);
    warnings.clear();
    warningKeys.clear();
    return drained;
  }

  /**
   * Clears the recovery-warning ledger without returning it. Called at
   * the start of each compile: a prior compile that threw after
   * recording warnings (the renderer's drain never ran, e.g. the
   * empty-recovery hard error) must not leak stale warnings into the
   * next compile on this context, nor suppress identical fresh
   * warnings via the stale dedupe keys. Also resets the per-compile
   * warning budgets so a reused context gets a fresh allowance.
   */
  public void resetWarnings() {
    warnings.clear();
    warningKeys.clear();
    warningEmitted.clear();
    warningSuppressed.clear();
    totalWarningEmitted = 0;
    totalWarningSuppressed = 0;
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
    Buffer buf = new Buffer(opts.indent(), opts.compress());
    buf.compat(opts.compat());
    return buf;
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
