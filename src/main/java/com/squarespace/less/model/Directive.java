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

import java.nio.file.Path;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


/**
 * A named CSS directive.
 */
public class Directive extends BaseNode {

  /**
   * Name of the directive.
   */
  protected final String name;

  /**
   * Value for this directive.
   */
  protected final Node value;

  /**
   * Filename this directive was defined in.
   */
  protected Path fileName;

  /**
   * Construct a directive with the given name and value.
   */
  public Directive(String name, Node value) {
    if (name == null || value == null) {
      throw new LessInternalException("Serious error: name/value cannot be null.");
    }
    this.name = name;
    this.value = value;
  }

  /**
   * Return the name of this directive.
   */
  public String name() {
    return name;
  }

  /**
   * Return the value for this directive.
   */
  public Node value() {
    return this.value;
  }

  /**
   * Return the path to the filename in which this directive was defined.
   */
  public Path fileName() {
    return fileName;
  }

  /**
   * Set the path to the filename in which this directive was defined.
   */
  public void fileName(Path path) {
    this.fileName = path;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    return new Directive(name, value.eval(env));
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return value.needsEval();
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.DIRECTIVE;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(name);
    if (value != null) {
      buf.append(' ');
      value.repr(buf);
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent().indent();
    value.modelRepr(buf);
    buf.decrIndent();
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
  public int hashCode() {
    return hashCode == 0 ? buildHashCode(name, value) : hashCode;
  }

}
