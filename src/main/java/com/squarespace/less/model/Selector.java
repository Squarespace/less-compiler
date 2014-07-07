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


public class Selector extends BaseNode {

  private static final int DEFAULT_CAPACITY = 4;

  protected List<Element> elements;

  protected boolean hasWildcard;

  protected boolean evaluate;

  public boolean hasWildcard() {
    return hasWildcard;
  }

  public void add(Element element) {
    elements = LessUtils.initList(elements, DEFAULT_CAPACITY);
    elements.add(element);
    evaluate |= element.needsEval();
    if (element.isWildcard()) {
      hasWildcard = true;
    }
  }

  public List<Element> elements() {
    return LessUtils.safeList(elements);
  }

  public int size() {
    return (elements == null) ? 0 : elements.size();
  }

  public boolean isEmpty() {
    return elements == null || (elements.size() == 0);
  }

  @Override
  public boolean needsEval() {
    return evaluate;
  }

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

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Selector) ? safeEquals(elements, ((Selector)obj).elements) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.SELECTOR;
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, elements);
    buf.decrIndent();
  }

}
