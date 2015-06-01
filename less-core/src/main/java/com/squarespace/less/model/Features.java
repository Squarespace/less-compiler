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
 * List of {@link Feature} expressions that are part of a {@link Media}.
 */
public class Features extends BaseNode {

  /**
   * List of feature expressions.
   */
  protected List<Node> features;

  /**
   * Flag indicating whether any of the feautures require evaluation.
   */
  protected boolean evaluate;

  /**
   * Adds a feature node.
   */
  public void add(Node node) {
    features = LessUtils.initList(features, 2);
    features.add(node);
    evaluate |= node.needsEval();
  }

  /**
   * Adds a list of features.
   */
  public void add(List<Node> nodes) {
    features = LessUtils.initList(features, nodes.size());
    for (Node node : nodes) {
      features.add(node);
      evaluate |= node.needsEval();
    }
  }

  /**
   * Returns the list of features.
   */
  public List<Node> features() {
    return LessUtils.safeList(features);
  }

  /**
   * Indicates whether this the feature list is empty.
   */
  public boolean isEmpty() {
    return features == null ? true : features.isEmpty();
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
    Features result = new Features();
    for (Node node : features) {
      result.add(node.eval(env));
    }
    return result;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.FEATURES;
  }

  /**
   * See {@link Node#repr(Buffer)}
   */
  @Override
  public void repr(Buffer buf) {
    if (features != null) {
      int size = features.size();
      for (int i = 0; i < size; i++) {
        if (i > 0) {
          buf.append(", ");
        }
        features.get(i).repr(buf);
      }
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
    ReprUtils.modelRepr(buf, "\n", true, features);
    buf.decrIndent();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Features) ? safeEquals(features, ((Features)obj).features) : false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

}
