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
import static com.squarespace.less.model.NodeType.QUOTED;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.exec.ExecEnv;


public class Quoted extends BaseNode {

  private static final int DEFAULT_CAPACITY = 3;

  private final char delim;

  private List<Node> parts;

  private boolean escaped;

  private boolean evaluate;

  public Quoted(char delim) {
    this(delim, false);
  }

  public Quoted(char delim, boolean escape) {
    this(delim, escape, null);
  }

  public Quoted(char delim, boolean escape, List<Node> parts) {
    this.delim = delim;
    this.escaped = escape;
    this.parts = parts;
    if (parts != null) {
      for (Node part : parts) {
        evaluate |= part.needsEval();
      }
    }
  }

  public char delimiter() {
    return delim;
  }

  public boolean escaped() {
    return escaped;
  }

  public List<Node> parts() {
    return parts;
  }

  public void setEscape(boolean flag) {
    this.escaped = flag;
  }

  public void append(Node node) {
    this.parts = LessUtils.initList(parts, DEFAULT_CAPACITY);
    this.parts.add(node);
    this.evaluate |= node.needsEval();
  }

  public Quoted copy() {
    Quoted res = new Quoted(delim);
    for (Node part : parts) {
      res.append(part);
    }
    return res;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Quoted) {
      Quoted other = (Quoted)obj;
      return delim == other.delim && escaped == other.escaped && safeEquals(parts, other.parts);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return QUOTED;
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
    int size = parts.size();
    List<Node> result = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      result.add(parts.get(i).eval(env));
    }
    return new Quoted(delim, escaped, result);
  }

  @Override
  public void repr(Buffer buf) {
    if (escaped) {
      buf.append('~');
    }
    buf.append(delim);
    int size = parts.size();
    for (int i = 0; i < size; i++) {
      parts.get(i).repr(buf);
    }
    buf.append(delim);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    posRepr(buf);
    buf.append(" delim=").append(delim);
    if (escaped) {
      buf.append(" [escaped]");
    }
    buf.append('\n').incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, parts);
    buf.decrIndent();
  }


}
