package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Import extends BaseNode {

  private Node path;
  
  private Path rootPath;
  
  private Path fileName;

  private Features features;
  
  private boolean once;
  
  public Import(Node path, Features features, boolean once) {
    this.path = path;
    this.features = features;
    this.once = once;
  }
  
  public Node path() {
    return path;
  }
  
  public Features features() {
    return features;
  }

  public boolean once() {
    return once;
  }
  
  public Path rootPath() {
    return rootPath;
  }
  
  public Path fileName() {
    return fileName;
  }
  
  public void rootPath(Path rootPath) {
    this.rootPath = rootPath;
  }
  
  public void fileName(Path fileName) {
    this.fileName = fileName;
  }
  
  public String renderPath(ExecEnv env) throws LessException {
    Node value = path;
    if (value.is(NodeType.URL)) {
      value = ((Url)value).value();
    }

    Context ctx = env.context();
    Quoted quoted = null;
    String rendered = null;
    if (value.is(NodeType.QUOTED)) {
      
      // Strip quote delimiters and render inner string. This technique allows
      // for variable substitution inside @import paths, which may or may not
      // be useful.
      //
      // Conversely, Less.js performs all importing during the parse phase, so
      // it has to assume all paths are bare strings.
      quoted = ((Quoted)value).copy();
      quoted.setEscape(true);
      rendered = ctx.render(quoted);

    } else {
      rendered = ctx.render(value);
    }
    fileName = rootPath != null ? rootPath.resolve(rendered) : Paths.get(rendered);
    return rendered;
  }
  
  @Override
  public boolean needsEval() {
    return path.needsEval() || (features != null && features.needsEval());
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    Import result = new Import(path.eval(env), features == null ? null : (Features)features.eval(env), once);
    result.rootPath(rootPath);
    result.fileName(fileName);
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Import) ? safeEquals(path, ((Import)obj).path) : false; 
  }
  
  @Override
  public NodeType type() {
    return NodeType.IMPORT;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append("@import");
    if (once) {
      buf.append("-once ");
    } else {
      buf.append(' ');
    }
    path.repr(buf);
    if (features != null) {
      buf.append(" ");
      features.repr(buf);
    }
    buf.append(";\n");
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    if (once) {
      buf.append(" [once] ");
    }
    buf.append('\n').incrIndent().indent();
    path.modelRepr(buf);
    buf.append('\n');
    if (features != null) {
      buf.indent();
      ReprUtils.modelRepr(buf, "\n", true, features.features());
    }
    buf.decrIndent();
  }
  
}
