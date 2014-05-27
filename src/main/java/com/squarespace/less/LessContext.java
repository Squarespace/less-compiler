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

import static com.squarespace.less.core.ExecuteErrorMaker.importError;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.BufferStack;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.FunctionTable;
import com.squarespace.less.exec.ImportRecord;
import com.squarespace.less.exec.MixinResolver;
import com.squarespace.less.exec.NodeRenderer;
import com.squarespace.less.exec.RenderEnv;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Stylesheet;


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

  private static final LessOptions DEFAULT_OPTS = new LessOptions();

  private final BufferStack bufferStack = new BufferStack(this);

  private final Map<Path, ImportRecord> importCache = new HashMap<>();

  private final MixinResolver mixinResolver = new MixinResolver();

  private final LessStats stats = new LessStats();

  private final LessOptions opts;

  private final Map<Path, Stylesheet> preCache;

  private final LessLoader loader;

  private LessCompiler compiler;

  private FunctionTable functionTable;

  private int mixinDepth;

  public LessContext() {
    this(DEFAULT_OPTS);
  }

  public LessContext(LessOptions opts) {
    this(opts, null);
  }

  public LessContext(LessOptions opts, LessLoader loader) {
    this(opts, loader, null);
  }

  public LessContext(LessOptions opts, LessLoader loader, Map<Path, Stylesheet> preCache) {
    this.opts = opts;
    this.preCache = preCache == null ? new HashMap<Path, Stylesheet>() : preCache;
    this.loader = loader == null ? new FilesystemLessLoader() : loader;
  }

  public LessOptions options() {
    return opts;
  }

  public MixinResolver mixinResolver() {
    return mixinResolver;
  }

  public void sanityCheck() {
    bufferStack.sanityCheck();
  }

  public void setCompiler(LessCompiler compiler) {
    this.compiler = compiler;
    this.functionTable = compiler.functionTable();
  }

  public Function findFunction(String symbol) {
    return (functionTable != null) ? functionTable.get(symbol) : null;
  }

  /**
   * Retrieves an external stylesheet. If not already cached, parse it and cache it.
   */
  public Stylesheet importStylesheet(String rawPath, Path rootPath, boolean once) throws LessException {
    List<Path> importPaths = opts.importPaths();
    ImportRecord record = null;
    Path path = null;

    if (rootPath != null) {
      path = rootPath.resolve(rawPath).toAbsolutePath().normalize();
      record = importCache.get(path);
    }

    // If not found relative to the sibling dir, search the import path.
    if (record == null && importPaths != null && !importPaths.isEmpty()) {
      for (Path importPath : importPaths) {
        path = importPath.resolve(rawPath).toAbsolutePath().normalize();
        record = importCache.get(path);
        if (record != null) {
          break;
        }
      }
    }

    // If the stylesheet has been imported and the 'onlyOnce' flag is not set, return it.
    // Otherwise return null, indicating to the caller that it has already been imported
    // once and the flag is enforced.
    if (record != null) {

      // Global "import once" flag. All imports are processed only once.
      if (opts.importOnce()) {
        return null;
      }

      if (!record.onlyOnce()) {
        stats.importDone(true);
      }
      return record.onlyOnce() ? null : record.stylesheeet().copy();
    }

    // If a pre-populated parsed stylesheet cache has been provided, use it.
    Stylesheet result = null;
    if (preCache != null) {
      if (rootPath != null) {
        path = rootPath.resolve(rawPath).toAbsolutePath().normalize();
        result = preCache.get(path);
      }
      if (result == null && importPaths != null && !importPaths.isEmpty()) {
        for (Path importPath : importPaths) {
          path = importPath.resolve(rawPath).toAbsolutePath().normalize();
          result = preCache.get(path);
          if (result != null) {
            break;
          }
        }
      }
    }

    // Else, ask the loader if the file exists and parse it.
    if (result == null) {
      path = resolvePath(rootPath, rawPath);
      if (path == null) {
        throw new LessException(importError(rawPath, "File cannot be found"));
      }
      result = compiler.parse(loader.load(path), this, path.getParent(), path.getFileName());
    }

    // Stick it in the cache if not already present.
    if (!importCache.containsKey(path)) {
      importCache.put(path, new ImportRecord(path, result, once));
    }
    stats.importDone(false);
    return result.copy();
  }

  /**
   * Search the rootPath and the importPaths if any, looking for a file that exists.
   */
  private Path resolvePath(Path rootPath, String rawPath) {
    Path path = null;
    if (rootPath != null) {
      path = rootPath.resolve(rawPath).toAbsolutePath().normalize();
      if (loader.exists(path)) {
        return path;
      }
    }
    List<Path> importPaths = opts.importPaths();
    if (importPaths == null || importPaths.isEmpty()) {
      return null;
    }
    for (Path importPath : importPaths) {
      path = importPath.resolve(rawPath).toAbsolutePath().normalize();
      if (loader.exists(path)) {
        return path;
      }
    }
    return null;
  }

  public LessStats stats() {
    return stats;
  }

  public Buffer acquireBuffer() {
    return bufferStack.acquireBuffer();
  }

  public void returnBuffer() {
    bufferStack.returnBuffer();
  }

  public Buffer newBuffer() {
    return new Buffer(opts);
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

  public String render(Node node) throws LessException {
    return NodeRenderer.render(this, node);
  }

  public void render(Buffer buf, Node node) throws LessException {
    NodeRenderer.render(buf, node);
  }

  public void enterMixin() {
    this.mixinDepth++;
  }

  public void exitMixin() {
    this.mixinDepth--;
  }

  public int mixinDepth() {
    return this.mixinDepth;
  }

}
