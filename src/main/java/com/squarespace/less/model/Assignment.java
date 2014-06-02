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

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


public class Assignment extends BaseNode {

  private final String name;

  private final Node value;

  public Assignment(String name, Node value) {
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

  @Override
  public NodeType type() {
    return NodeType.ASSIGNMENT;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Assignment) {
      Assignment other = (Assignment)obj;
      return safeEquals(name, other.name) && safeEquals(value, other.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean needsEval() {
    return value.needsEval();
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    return value.needsEval() ? new Assignment(name, value.eval(env)) : this;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(name).append('=');
    value.repr(buf);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" name=").append(name).append('\n');
    if (value != null) {
      buf.incrIndent();
      buf.indent();
      value.modelRepr(buf);
      buf.decrIndent();
    }
  }

}
