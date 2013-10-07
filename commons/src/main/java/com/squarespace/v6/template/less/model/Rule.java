package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;
import static com.squarespace.v6.template.less.model.NodeType.RULE;

import java.nio.file.Path;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Rule extends BaseNode {

  private Node property;
  
  private Node value;
  
  private boolean important;
  
  private Path fileName;
  
  public Rule(Node property, Node value) {
    this(property, value, false);
  }
  
  public Rule(Node property, Node value, boolean important) {
    this.property = property;
    this.value = value;
    this.important = important;
  }
  
  public Node property() {
    return property;
  }
  
  public Node value() {
    return value;
  }
  
  public boolean important() {
    return important;
  }

  public void markImportant(boolean flag) {
    this.important = flag;
  }
  
  public Path fileName() {
    return fileName;
  }
  
  public void fileName(Path path) {
    this.fileName = path;
  }
  
  public Rule copy() {
    Rule rule = new Rule(property, value, important);
    rule.fileName = fileName;
    return rule;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Rule) {
      Rule other = (Rule)obj;
      return safeEquals(important, other.important)
          && safeEquals(property, other.property)
          && safeEquals(value, other.value);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return RULE;
  }
  
  @Override
  public boolean needsEval() {
    return value.needsEval();
  }
  
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    return new Rule(property, value.eval(env), important);
  }

  @Override
  public void repr(Buffer buf) {
    property.repr(buf);
    buf.append(": ");
    value.repr(buf);
    if (important) {
      buf.append(" !important");
    }
    buf.append(";\n");
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n').incrIndent().indent();
    property.modelRepr(buf);
    if (important) {
      buf.append("!important");
    }
    buf.append('\n');
    if (value != null) {
      buf.indent();
      value.modelRepr(buf);
    }
    buf.decrIndent();
  }
  
}
