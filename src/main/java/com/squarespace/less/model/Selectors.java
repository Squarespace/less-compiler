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

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a comma-separated list of Selector nodes that forms
 * the header of a Ruleset.
 */
public class Selectors extends BaseNode {

  protected List<Selector> selectors;

  protected boolean evaluate;

  public Selectors() {
  }

  public Selectors(List<Selector> selectors) {
    this.selectors = selectors;
  }

  public boolean isEmpty() {
    return selectors == null ? true : selectors.isEmpty();
  }

  public void add(Selector selector) {
    selectors = LessUtils.initList(selectors, 2);
    selectors.add(selector);
    evaluate |= selector.needsEval();
  }

  public List<Selector> selectors() {
    return LessUtils.safeList(selectors);
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
    Selectors result = new Selectors();
    for (Selector selector : selectors) {
      result.add((Selector)selector.eval(env));
    }
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Selectors) ? LessUtils.safeEquals(selectors, ((Selectors)obj).selectors) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.SELECTORS;
  }

  @Override
  public void repr(Buffer buf) {
    int size = selectors.size();
    boolean emitted = false;
    for (int i = 0; i < size; i++) {
      if (emitted) {
        buf.append(',');
        if (!buf.compress()) {
          buf.append('\n').indent();
        }
      }
      Selector selector = selectors.get(i);
      if (selector != null) {
        emitted = true;
        reprSelector(buf, selector);
      }
    }
  }

  public static void reprSelector(Buffer buf, Selector selector) {
    List<Element> elements = selector.elements();
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      reprElement(buf, elements.get(i), i == 0);
    }
  }

  private static void reprElement(Buffer buf, Element element, boolean isFirst) {
    Combinator combinator = element.combinator();
    if (combinator != null) {
      boolean isDescendant = combinator == Combinator.DESC;
      char ch = combinator.getChar();
      if (isFirst) {
        if (!isDescendant) {
          buf.append(ch);
        }
      } else {
        if (!buf.compress() && !isDescendant) {
          buf.append(' ');
        }
        if (!isDescendant || !CharClass.whitespace(buf.prevChar())) {
          buf.append(ch);
        }
      }
      if (!buf.compress() && !element.isWildcard() && !CharClass.whitespace(buf.prevChar())) {
        buf.append(' ');
      }
    }
    element.repr(buf);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, selectors);
    buf.decrIndent();
  }

}
