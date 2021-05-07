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

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.LessUtils.safeEquals;

import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * List of Condition that follow a Mixin.
 */
public class Guard implements Node {

  /**
   * List of conditions to be evaluated.
   */
  protected List<Condition> conditions;

  /**
   * Constructs a guard with no conditions.
   */
  public Guard() {
  }

  /**
   * Adds a condition to the guard expression.
   */
  public void add(Condition cond) {
    conditions = LessUtils.initList(conditions, 2);
    conditions.add(cond);
  }

  /**
   * Adds all the given conditions to the guard expression.
   */
  public void addAll(Condition ... elements) {
    conditions = Arrays.asList(elements);
  }

  /**
   * Returns the list of conditions in the guard expression.
   */
  public List<Condition> conditions() {
    return LessUtils.safeList(conditions);
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return true;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    Node result = FALSE;
    List<Condition> _conditions = this.conditions();
    int size = _conditions.size();
    for (int i = 0; i < size; i++) {
      Condition condition = _conditions.get(i);
      result = condition.eval(env);
      if (result instanceof True) {
        break;
      }
    }
    return result;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.GUARD;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    int size = conditions.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      conditions.get(i).repr(buf);
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    if (conditions != null) {
      ReprUtils.modelRepr(buf, "\n", true, conditions);
    } else {
      buf.append("<null>");
    }
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Guard) {
      Guard other = (Guard)obj;
      return safeEquals(conditions, other.conditions);
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

}
