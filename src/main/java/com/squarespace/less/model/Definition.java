package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.nio.file.Path;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.ExecuteErrorMaker;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


/**
 * Special rule which represents a variable definition.
 */
public class Definition extends BaseNode {

  private final String name;

  private final Node value;

  // Flag to detect late-binding circular references and raise an error.
  private boolean evaluating;

  private Path fileName;

  private String warnings;

  public Definition(Variable variable, Node value) {
    this(variable.name(), value);
  }

  public Definition(String name, Node value) {
    if (name == null || value == null) {
      throw new LessInternalException("Serious error: name/value cannot be null.");
    }
    this.name = name;
    this.value = value;
  }

  public String name() {
    return name;
  }

  public Node value() {
    return value;
  }

  public Path fileName() {
    return fileName;
  }

  public void fileName(Path path) {
    this.fileName = path;
  }

  public String warnings() {
    return warnings;
  }

  public void warnings(String warnings) {
    this.warnings = warnings;
  }

  /**
   * Resolve the value for this definition.
   */
  public Node dereference(ExecEnv env) throws LessException {
    if (evaluating) {
      throw new LessException(ExecuteErrorMaker.varCircularRef(env));
    }
    evaluating = true;
    Node result = value.eval(env);
    evaluating = false;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Definition) {
      Definition other = (Definition)obj;
      return safeEquals(name, other.name) && safeEquals(value, other.value);
    }
    return false;
  }

  @Override
  public NodeType type() {
    return NodeType.DEFINITION;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    // Late binding. Definitions must be explicitly dereferenced to evaluate them.
    return this;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(name).append(": ");
    value.repr(buf);
    buf.append(";\n");
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(name).append('\n');
    if (value != null) {
      buf.incrIndent().indent();
      value.modelRepr(buf);
      buf.decrIndent().append('\n');
    }
  }

}
