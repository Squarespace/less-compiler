package com.squarespace.v6.template.less;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.FunctionTable;
import com.squarespace.v6.template.less.exec.MixinResolver;
import com.squarespace.v6.template.less.exec.RenderEnv;
import com.squarespace.v6.template.less.exec.Renderer;
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

  private Renderer renderer;

  private FunctionTable functionTable;

  private MixinResolver mixinResolver;
  
  private LessStats stats;
  
  public Context() {
    this(DEFAULT_OPTS);
  }
  
  public Context(Options opts) {
    this.opts = opts;
    this.renderer = new Renderer();
    this.mixinResolver = new MixinResolver();
    this.stats = new LessStats();
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
  
  // XXX: move to LessImporter
  public Stylesheet parseImport(String rawPath) throws LessException {
    Path root = FileSystems.getDefault().getPath(opts.importRoot());
    Path path = root.resolve(rawPath);
    return compiler.parse(readFile(path), this);
  }
  
  private String readFile(Path path) {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
      
    } catch (IOException e) {
      String message = String.format("Failure to read from '%s'", path);
      throw new RuntimeException(message + ": " + e.getMessage(), e);
    }
  }
  
  public LessStats stats() {
    return stats;
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
  
  public Renderer renderer() {
    return renderer;
  }
  
}
