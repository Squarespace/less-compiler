package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import java.nio.file.Path;

import com.squarespace.v6.template.less.core.Buffer;


public class Directive extends BaseNode {

  private final String name;
  
  private final Node value;
  
  private Path fileName;
  
  public Directive(String name, Node value) {
    if (name == null || value == null) {
      throw new IllegalArgumentException("Serious error: name/value cannot be null.");
    }
    this.name = name;
    this.value = value;
  }
  
  public String name() {
    return name;
  }
  
  public Node value() {
    return this.value;
  }
  
  public Path fileName() {
    return fileName;
  }
  
  public void fileName(Path path) {
    this.fileName = path;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Directive) {
      Directive other = (Directive)obj;
      return safeEquals(name, other.name) && safeEquals(value, other.value);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.DIRECTIVE;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append(name);
    if (value != null) {
      buf.append(' ');
      value.repr(buf);
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent().indent();
    value.modelRepr(buf);
    buf.decrIndent();
  }
  
}
