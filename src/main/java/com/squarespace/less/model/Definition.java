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

import static com.squarespace.less.core.ExecuteErrorMaker.varCircularRef;
import static com.squarespace.less.core.LessUtils.safeEquals;

import java.nio.file.Path;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


// An experimental feature is to detect and skip circular references,
// obtaining the definition from a higher scope.  This could be
// activated with a pragma in future releases.
//
// For example:
//
// @foo: 1px;
//
//  .parent {
//     // global @foo
//     @bar: @foo + 2px;
//
//     .child {
//         // @foo: 1px (global @foo) + 3px (.parent @bar)
//         @foo: @foo + @bar;
//         prop: @foo;
//     }
// }
//
// Result:
//
// .parent {
//    prop: 4px;
// }



/**
 * Special rule which represents a variable definition.
 */
public class Definition extends StructuralNode {

  /**
   * Name of the variable definition.
   */
  protected final String name;

  /**
   * Value of the variable definition.
   */
  protected final Node value;

  /**
   * Flag to detect circular references and raise an error.
   */
  protected boolean evaluating;

  /**
   * Filename in which this variable is defined.
   */
  protected Path fileName;

  /**
   * Warnings associated with this variable definition.
   */
  protected String warnings;

  /**
   * Constructs a definition with the {@code variable}'s name and the given value.
   */
  public Definition(Variable variable, Node value) {
    this(variable.name(), value);
  }

  /**
   * Constructs a definition with the given name and value.
   */
  public Definition(String name, Node value) {
    if (name == null || value == null) {
      throw new LessInternalException("Serious error: name/value cannot be null.");
    }
    this.name = name;
    this.value = value;
  }

  /**
   * Constructs a definition by copying {@code orig} and substituting a new value
   */
  protected Definition(Definition orig, Node newValue) {
    this(orig.name(), newValue);
    this.fileName = orig.fileName;
    copyStructure(orig);
  }

  /**
   * Creates a copy of this definition, substituting a new value.
   */
  public Definition copy(Node newValue) {
    return new Definition(this, newValue);
  }

  public String name() {
    return name;
  }

  public Node value() {
    return value;
  }

  /**
   * Indicates whether this definition is currently being evaluated. Helps
   * detect and skip over circular references.
   */
  public boolean evaluating() {
    return evaluating;
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
    // TODO: future pragma to detect and skip circular definitions,
    // looking in a higher scope. remove the following line.
    // see ExecEnv.resolveDefinition
    if (evaluating) {
      throw new LessException(varCircularRef(env));
    }

    // Mark as 'evaluating' so that we can detect circular references.
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
  public String toString() {
    return ModelUtils.toString(this);
  }

  @Override
  public int hashCode() {
    return ModelUtils.notHashable();
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.DEFINITION;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    // Late binding. Definitions must be explicitly dereferenced to evaluate them.
    return this;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    buf.append(name).append(": ");
    value.repr(buf);
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    bodyRepr(buf);
  }

  /**
   * Constructs the body of the definition, used in {@link #modelRepr(Buffer)}
   */
  protected void bodyRepr(Buffer buf) {
    buf.append(' ').append(name).append('\n');
    if (value != null) {
      buf.incrIndent().indent();
      value.modelRepr(buf);
      buf.decrIndent().append('\n');
    }
  }

}
