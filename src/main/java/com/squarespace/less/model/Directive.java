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


public class Directive extends BaseNode {

  protected final String name;

  protected final Node value;

  protected Path fileName;

  public Directive(String name, Node value) {
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
    return this.value;
  }

  public Path fileName() {
    return fileName;
  }

  public void fileName(Path path) {
    this.fileName = path;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    return new Directive(name, value.eval(env));
  }

  @Override
  public boolean needsEval() {
    return value.needsEval();
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
    return super.hashCode();
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
    posRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent().indent();
    value.modelRepr(buf);
    buf.decrIndent();
  }

}
