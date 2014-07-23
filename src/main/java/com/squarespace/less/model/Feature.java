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
import com.squarespace.less.exec.ExecEnv;


public class Feature extends BaseNode {

  private final Node property;

  private final Node value;

  public Feature(Node property, Node value) {
    this.property = property;
    this.value = value;
  }

  public Node property() {
    return property;
  }

  public Node value() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Feature) {
      Feature other = (Feature)obj;
      return safeEquals(property, other.property) && safeEquals(value, other.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.FEATURE;
  }

  @Override
  public boolean needsEval() {
    return property.needsEval() || value.needsEval();
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!needsEval()) {
      return this;
    }
    Feature result = new Feature(property.eval(env), value.eval(env));
    result.copyBase(this);
    return result;
  }

  @Override
  public void repr(Buffer buf) {
    property.repr(buf);
    buf.append(": ");
    value.repr(buf);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n').incrIndent().indent();
    property.modelRepr(buf);
    buf.append('\n');
    value.modelRepr(buf);
    buf.decrIndent();
  }

}
