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

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents one selector in a selector set.
 */
public class Selector extends BaseNode {

  /**
   * Default capacity for the selector's element list.
   */
  private static final int DEFAULT_CAPACITY = 4;

  /**
   * List of elements in this selector.
   */
  protected List<Element> elements;

  /**
   * {@link Mixin} path this selector maps to.
   */
  protected List<String> mixinPath;

  /**
   * Indicates whether this selector list contains a wildcard element.
   */
  protected boolean hasWildcard;

  /**
   * Indicates whether one of the elements in the list requires evaluation.
   */
  protected boolean evaluate;

  /**
   * Indicates whether the selector list contains a wildcard element.
   */
  public boolean hasWildcard() {
    return hasWildcard;
  }

  /**
   * Adds an element to the selector.
   */
  public void add(Element element) {
    elements = LessUtils.initList(elements, DEFAULT_CAPACITY);
    elements.add(element);
    evaluate |= element.needsEval();
    if (element.isWildcard()) {
      hasWildcard = true;
    }
  }

  /**
   * Returns the list of elements in this selector.
   */
  public List<Element> elements() {
    return LessUtils.safeList(elements);
  }

  /**
   * Returns the segmented {@link Mixin} path corresponding to this selector.
   */
  public List<String> mixinPath() {
    return mixinPath;
  }

  /**
   * Sets the segmented {@link Mixin} path corresponding to this selector.
   */
  public void mixinPath(List<String> mixinPath) {
    this.mixinPath = mixinPath;
  }

  /**
   * Number of elements in the selector.
   */
  public int size() {
    return (elements == null) ? 0 : elements.size();
  }

  /**
   * Indicates whether the selector has no elements.
   */
  public boolean isEmpty() {
    return elements == null || elements.isEmpty();
  }

  /**
   * See {@link Node#needsEval()}
   */
  @Override
  public boolean needsEval() {
    return evaluate;
  }

  /**
   * See {@link Node#eval(ExecEnv)}
   */
  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!evaluate) {
      return this;
    }
    Selector result = new Selector();
    for (Element elem : elements) {
      result.add((Element)elem.eval(env));
    }
    return result;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.SELECTOR;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    Selectors.reprSelector(buf, this);
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
    ReprUtils.modelRepr(buf, "\n", true, elements);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Selector) ? safeEquals(elements, ((Selector)obj).elements) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
