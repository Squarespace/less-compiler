/**
 * Copyright, 2015, Squarespace, Inc.
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

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a group of {@link Extend} elements.
 */
public class ExtendList extends BaseNode {

  /**
   * Indicates if this extend list is at rule level or not.
   */
  private final boolean atRuleLevel;

  /**
   * Zero or more extend elements.
   */
  private List<Extend> values;

  /**
   * Constructs an extend list, optionally specifying the extend
   * expression is at rule level.
   */
  public ExtendList(boolean atRuleLevel) {
    this.atRuleLevel = atRuleLevel;
  }

  /**
   * Indicates whether this extend list is empty.
   */
  public boolean isEmpty() {
    return values == null || values.isEmpty();
  }

  /**
   * @see Node#type()
   */
  @Override
  public NodeType type() {
    return NodeType.EXTEND_LIST;
  }

  /**
   * Adds an {@link Extend} element to the list.
   */
  public void add(Extend extend) {
    this.values = LessUtils.initList(values, 3);
    this.values.add(extend);
  }

  /**
   * @see Node#eval(ExecEnv)
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    return super.eval(env);
  }

  /**
   * @see Node#repr(Buffer)
   */
  @Override
  public void repr(Buffer buf) {
    if (atRuleLevel) {
      buf.append('&');
    }
    buf.append(":extend(");
    if (!isEmpty()) {
      int size = values.size();
      for (int i = 0; i < size; i++) {
        if (i > 0) {
          buf.listSep();
        }
        values.get(i).repr(buf);
      }
    }
    buf.append(')');
  }

  /**
   * @see Node#modelRepr(Buffer)
   */
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n').incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, values);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ExtendList) {
      ExtendList other = (ExtendList)obj;
      return LessUtils.safeEquals(values, other.values);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
