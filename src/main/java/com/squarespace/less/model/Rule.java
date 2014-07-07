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

package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.RULE;

import java.nio.file.Path;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.ExecEnv;


public class Rule extends BaseNode {

  protected final Node property;

  protected final Node value;

  protected boolean important;

  protected Path fileName;

  protected String warnings;

  public Rule(Node property, Node value) {
    this(property, value, false);
  }

  public Rule(Node property, Node value, boolean important) {
    this.property = property;
    this.value = value;
    this.important = important;
  }

  public Rule copy(Node newValue, boolean important) {
    Rule result = new Rule(property, newValue, important);
    result.copyBase(this);
    result.fileName(fileName);
    return result;
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

  public String warnings() {
    return warnings;
  }

  public void warnings(String warnings) {
    this.warnings = warnings;
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
  public int hashCode() {
    return super.hashCode();
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
    Rule result = new Rule(property, value.eval(env), important);
    result.fileName(fileName);
    result.copyBase(this);
    return result;
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
    posRepr(buf);
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
