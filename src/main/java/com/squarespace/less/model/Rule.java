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


/**
 * A CSS rule, e.g.  font-size: 12px !important;
 */
public class Rule extends BaseNode {

  /**
   * Property name for this rule.
   */
  protected final Node property;

  /**
   * Value for this rule.
   */
  protected final Node value;

  /**
   * Marks the rule as !important.
   */
  protected boolean important;

  /**
   * Path to the file in which this rule was defined.
   */
  protected Path fileName;

  /**
   * Warnings attached to this rule.
   */
  protected String warnings;

  /**
   * Constructs a rule with the given property and value.
   */
  public Rule(Node property, Node value) {
    this(property, value, false);
  }

  /**
   * Constructs a rule with the given property and value, and optionally
   * set the value of the {@code important} flag.
   */
  public Rule(Node property, Node value, boolean important) {
    this.property = property;
    this.value = value;
    this.important = important;
  }

  /**
   * Copies a rule, swapping in a new value, and a new value for the
   * {@code important} flag.
   */
  public Rule copy(Node newValue, boolean important) {
    Rule result = new Rule(property, newValue, important);
    result.copyBase(this);
    result.fileName(fileName);
    return result;
  }

  /**
   * Returns the rule's property.
   */
  public Node property() {
    return property;
  }

  /**
   * Returns the rule's value.
   */
  public Node value() {
    return value;
  }

  /**
   * Indicates whether the rule is marked important.
   */
  public boolean important() {
    return important;
  }

  /**
   * Marks the rule as important.
   */
  public void markImportant(boolean flag) {
    this.important = flag;
  }

  /**
   * Returns the path to the file in which this rule is defined.
   */
  public Path fileName() {
    return fileName;
  }

  /**
   * Sets the path to the file in which this rule is defined.
   */
  public void fileName(Path path) {
    this.fileName = path;
  }

  /**
   * Returns the warnings associated with this rule.
   */
  public String warnings() {
    return warnings;
  }

  /**
   * Attaches the warnings string to this rule.
   */
  public void warnings(String warnings) {
    this.warnings = warnings;
  }

  /**
   * Copies this rule.
   */
  public Rule copy() {
    Rule rule = new Rule(property, value, important);
    rule.fileName = fileName;
    return rule;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return RULE;
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return value.needsEval();
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
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

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    property.repr(buf);
    buf.append(':');
    if (!buf.compress()) {
      buf.append(' ');
    }
    value.repr(buf);
    if (important) {
      buf.append(" !important");
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
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

}
