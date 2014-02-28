package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.core.ExecuteErrorMaker.importError;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.BufferStack;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.FunctionTable;
import com.squarespace.v6.template.less.exec.ImportRecord;
import com.squarespace.v6.template.less.exec.MixinResolver;
import com.squarespace.v6.template.less.exec.NodeRenderer;
import com.squarespace.v6.template.less.exec.RenderEnv;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Stylesheet;


/** 
 * Context for a single LESS parse/compile operation.  Used by implementation classes
 * to obtain access to compiler-wide state:
 *  - compile options
 *  - node renderer
 *  - reusable compiler
 *  - reusable buffer stack
 *  etc.
 */
public class Context {

  private static final Options DEFAULT_OPTS = new Options();

  private Options opts;

  private LessCompiler compiler;

  private BufferStack bufferStack;
  
  private NodeRenderer renderer;

  private FunctionTable functionTable;

  private MixinResolver mixinResolver;
  
  private LessStats stats;
  
  private Map<Path, ImportRecord> importCache;

  private Map<Path, Stylesheet> preCache;
  
  private LessLoader loader;
  
  private int mixinDepth;
  
  public Context() {
    this(DEFAULT_OPTS);
  }
  
  public Context(Options opts) {
    this(opts, null);
  }
  
  public Context(Options opts, LessLoader loader) {
    this(opts, loader, null);
  }
  
  public Context(Options opts, LessLoader loader, Map<Path, Stylesheet> preCache) {
    this.opts = opts;
    this.bufferStack = new BufferStack(this);
    this.renderer = new NodeRenderer();
    this.mixinResolver = new MixinResolver();
    this.stats = new LessStats();
    this.importCache = new HashMap<>();
    this.preCache = preCache == null ? new HashMap<Path, Stylesheet>() : preCache;
    this.loader = loader == null ? new FilesystemLessLoader() : loader;
  }
  
  public Options options() {
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
  
  public void setFunctionTable(FunctionTable table) {
    this.functionTable = table;
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
  
  public ErrorInfo newError(ErrorType type) {
    return new ErrorInfo(type);
  }
  
  public String render(Node node) throws LessException {
    return renderer.render(this, node);
  }
  
  public void render(Buffer buf, Node node) throws LessException {
    renderer.render(buf, node);
  }
  
  public NodeRenderer renderer() {
    return renderer;
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
