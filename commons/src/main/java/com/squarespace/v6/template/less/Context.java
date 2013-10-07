package com.squarespace.v6.template.less;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
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
 * Context for a single LESS parse/compile operation.  Useful for accessing
 * compiler-wide state.
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
  
  private ScriptLoader loader;
  
  public Context() {
    this(DEFAULT_OPTS);
  }
  
  public Context(Options opts) {
    this(opts, null);
  }
  
  public Context(Options opts, ScriptLoader loader) {
    this(opts, loader, null);
  }
  
  public Context(Options opts, ScriptLoader loader, Map<Path, Stylesheet> preCache) {
    this.opts = opts;
    this.bufferStack = new BufferStack(this);
    this.renderer = new NodeRenderer();
    this.mixinResolver = new MixinResolver();
    this.stats = new LessStats();
    this.importCache = new HashMap<>();
    this.preCache = preCache == null ? new HashMap<Path, Stylesheet>() : preCache;
    this.loader = loader == null ? new FilesystemScriptLoader() : loader;
  }
  
  public Options options() {
    return opts;
  }
  
  public MixinResolver mixinResolver() {
    return mixinResolver;
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
    if (rootPath == null) {
      rootPath = FileSystems.getDefault().getPath(opts.importRoot());
    }
    Path path = rootPath.resolve(rawPath).normalize();
    ImportRecord record = importCache.get(path);
    
    // If the stylesheet has been imported and the 'onlyOnce' flag is not set, return it.
    // Otherwise return null, indicating to the caller that it has already been imported
    // once and the flag is enforced.
    if (record != null) {
      return record.onlyOnce() ? null : record.stylesheeet().copy();
    }

    // If a pre-populated parsed stylesheet cache has been provided, use it.
    Stylesheet result = null;
    if (preCache != null) {
      result = preCache.get(path);
    }
    
    if (result == null) {
      result = compiler.parse(loader.load(path), this, path.getParent(), path.getFileName());
    }
    if (!importCache.containsKey(path)) {
      importCache.put(path, new ImportRecord(result, once));
    }
    return result.copy();
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
  
}
