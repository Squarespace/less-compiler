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
import com.squarespace.less.exec.SelectorUtils;


public class Ruleset extends BlockNode {

  private final Selectors selectors;

  private boolean evaluating;

  private List<List<String>> mixinPaths;

  public Ruleset() {
    this.selectors = new Selectors();
  }

  public Ruleset(Selectors selectors) {
    this(selectors, new Block());
  }

  public Ruleset(Selectors selectors, Block block) {
    super(block);
    this.selectors = selectors;
    addMixinPaths(selectors);
  }

  public Ruleset copy(ExecEnv env) throws LessException {
    Ruleset result = new Ruleset((Selectors) selectors.eval(env), block.copy());
    result.mixinPaths = mixinPaths;
    result.fileName = fileName;
    if (originalBlockNode != null) {
      result.originalBlockNode = originalBlockNode;
    }
    return result;
  }

  public Selectors selectors() {
    return selectors;
  }

  public List<List<String>> mixinPaths() {
    return LessUtils.safeList(mixinPaths);
  }

  public void enter() {
    evaluating = true;
  }

  public void exit() {
    evaluating = false;
  }

  public boolean evaluating() {
    return evaluating;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Ruleset) {
      Ruleset other = (Ruleset)obj;
      return safeEquals(selectors, other.selectors) && super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public void add(Node node) {
    if (node.is(NodeType.SELECTOR)) {
      Selector selector = (Selector)node;
      selectors.add(selector);
      addMixinPaths(selector);

    } else {
      super.add(node);
    }
  }

  @Override
  public NodeType type() {
    return NodeType.RULESET;
  }

  @Override
  public void repr(Buffer buf) {
    selectors.repr(buf);
    buf.append(" {\n");
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    buf.indent().append("}\n");
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent().indent();
    selectors.modelRepr(buf);
    buf.append('\n');
    super.modelRepr(buf);
    buf.decrIndent().append('\n');
  }

  private void addMixinPaths(Selectors selectors) {
    for (Selector selector : selectors.selectors()) {
      addMixinPaths(selector);
    }
  }

  private void addMixinPaths(Selector selector) {
    List<String> path = SelectorUtils.renderMixinSelector(selector);
    if (path != null) {
      addMixinPath(path);
    }
  }

  private void addMixinPath(List<String> path) {
    mixinPaths = LessUtils.initList(mixinPaths, 2);
    mixinPaths.add(path);
  }

}

